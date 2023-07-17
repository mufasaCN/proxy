package cn.spf.proxy.netty;

import com.alibaba.fastjson2.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.util.Attribute;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.context.ApplicationContext;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/7/17 15:26
 */
@Slf4j
public class HttpClient {
	private static final long LockTimeoutMillis = 3000;
	
	private DefaultEventExecutorGroup defaultEventExecutorGroup;
	
	private final Bootstrap bootstrap = new Bootstrap();
	
	private final Lock lockChannelTables = new ReentrantLock();
	
	private static URI uri;
	private static String scheme;
	private static String hostname;
	private static Integer port;
	static {
		try {
			uri = new URI("http://pv.sohu.com/cityjson?ie=utf-8");
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}
	
	private final ConcurrentHashMap<String, ChannelWrapper> channelTables =
			new ConcurrentHashMap<>();
	
	public void start() {
		
		scheme = uri.getScheme() == null? "http" : uri.getScheme();
		hostname = uri.getHost() == null? "127.0.0.1" : uri.getHost();
		port = uri.getPort();
		if (port == -1) {
			if ("http".equalsIgnoreCase(scheme)) {
				port = 80;
			} else if ("https".equalsIgnoreCase(scheme)) {
				port = 443;
			}
		}
		
		if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
			log.error("只支持HTTP(S)协议.");
			return;
		}
		
		// Configure SSL context if necessary.
		final boolean ssl = "https".equalsIgnoreCase(scheme);
		final SslContext sslCtx;
		if (ssl) {
			sslCtx = null;
			//SslContextBuilder.forClient()
			// .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
		} else {
			sslCtx = null;
		}
		this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(4,
				new ThreadFactory() {
					private AtomicInteger threadIndex = new AtomicInteger(0);
					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, "NettyClientWorkerThread_" + this.threadIndex.incrementAndGet());
					}
				});
		
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		
		this.bootstrap.group(workerGroup).channel(NioSocketChannel.class)//
				.option(ChannelOption.TCP_NODELAY, true)
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.SO_SNDBUF, 65535)
				.option(ChannelOption.SO_RCVBUF, 65535)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ch.pipeline().addFirst(
								new Socks5ProxyHandler(new InetSocketAddress("49.51.67.180",2333),
										"tuiguang3-zone-sto316761-region-us-session-123719kgk",
										"tuiguang3"))
								.addLast(//
										defaultEventExecutorGroup, //
										new HttpClientCodec(),
										new HttpContentDecompressor(),
										new HttpClientInHandler());
					}
				});
	}
	class ChannelWrapper {
		private final ChannelFuture channelFuture;
		public ChannelWrapper(ChannelFuture channelFuture) {
			this.channelFuture = channelFuture;
		}
		public boolean isOK() {
			return (this.channelFuture.channel() != null && this.channelFuture.channel().isActive());
		}
		private Channel getChannel() {
			return this.channelFuture.channel();
		}
		public ChannelFuture getChannelFuture() {
			return channelFuture;
		}
	}
	
	public Channel getAndCreateChannel(final String addr) throws InterruptedException {
		ChannelWrapper cw = this.channelTables.get(addr);
		if (cw != null && cw.isOK()) {
			return cw.getChannel();
		}
		
		return this.createChannel(addr);
	}
	
	private Channel createChannel(final String addr) throws InterruptedException {
		ChannelWrapper cw = this.channelTables.get(addr);
		if (cw != null && cw.isOK()) {
			return cw.getChannel();
		}
		
		// 进入临界区后，不能有阻塞操作，网络连接采用异步方式
		if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
			try {
				boolean createNewConnection = false;
				cw = this.channelTables.get(addr);
				if (cw != null) {
					// channel正常
					if (cw.isOK()) {
						return cw.getChannel();
					}
					// 正在连接，退出锁等待
					else if (!cw.getChannelFuture().isDone()) {
						createNewConnection = false;
					}
					// 说明连接不成功
					else {
						this.channelTables.remove(addr);
						createNewConnection = true;
					}
				}
				// ChannelWrapper不存在
				else {
					createNewConnection = true;
				}
				
				if (createNewConnection) {
					String[] splitValue=addr.split("\\+");
					
					ChannelFuture channelFuture = this.bootstrap.connect(RemotingHelper.string2SocketAddress(splitValue[0])).sync();
					
					//log.info("createChannel: begin to connect remote host[{}] asynchronously", addr);
					cw = new ChannelWrapper(channelFuture);
					this.channelTables.put(addr, cw);
				}
			}
			catch (Exception e) {
				log.error("创建Channel: 创建channel异常："+e);
			}
			finally {
				this.lockChannelTables.unlock();
			}
		}
		else {
			//log.warn("createChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
		}
		
		if (cw != null) {
			return cw.getChannel();
		}
		
		return null;
	}
	
	public void closeChannel(final String addr, final Channel channel) {
		if (null == channel)
			return;
		
		final String addrRemote = null == addr ? RemotingHelper.parseChannelRemoteAddr(channel) : addr;
		
		try {
			if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
				try {
					boolean removeItemFromTable = true;
					final ChannelWrapper prevCW = this.channelTables.get(addrRemote);
					
					//log.info("closeChannel: begin close the channel[{}] Found: {}", addrRemote, (prevCW != null));
					
					if (null == prevCW) {
						//log.info("closeChannel: the channel[{}] has been removed from the channel table before", addrRemote);
						removeItemFromTable = false;
					}
					else if (prevCW.getChannel() != channel) {
						//log.info("closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.", addrRemote);
						removeItemFromTable = false;
					}
					
					if (removeItemFromTable) {
						this.channelTables.remove(addrRemote);
						//log.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
					}
					
					RemotingHelper.closeChannel(channel);
				}
				catch (Exception e) {
					//log.error("closeChannel: close the channel exception", e);
				}
				finally {
					this.lockChannelTables.unlock();
				}
			}
			else {
				//log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
			}
		}
		catch (InterruptedException e) {
			//log.error("closeChannel exception", e);
		}
	}
	
	public void closeChannel(final Channel channel) {
		if (null == channel)
			return;
		try {
			if (this.lockChannelTables.tryLock(LockTimeoutMillis, TimeUnit.MILLISECONDS)) {
				try {
					boolean removeItemFromTable = true;
					ChannelWrapper prevCW = null;
					String addrRemote = null;
					for (String key : channelTables.keySet()) {
						ChannelWrapper prev = this.channelTables.get(key);
						if (prev.getChannel() != null) {
							if (prev.getChannel() == channel) {
								prevCW = prev;
								addrRemote = key;
								break;
							}
						}
					}
					
					if (null == prevCW) {
						//log.info("eventCloseChannel: the channel[{}] has been removed from the channel table before", addrRemote);
						removeItemFromTable = false;
					}
					
					if (removeItemFromTable) {
						this.channelTables.remove(addrRemote);
						//log.info("closeChannel: the channel[{}] was removed from channel table", addrRemote);
						RemotingHelper.closeChannel(channel);
					}
				}
				catch (Exception e) {
					//log.error("closeChannel: close the channel exception", e);
				}
				finally {
					this.lockChannelTables.unlock();
				}
			}
			else {
				//log.warn("closeChannel: try to lock channel table, but timeout, {}ms", LockTimeoutMillis);
			}
		}
		catch (InterruptedException e) {
			//log.error("closeChannel exception", e);
		}
	}
	
	public void sendSyncImpl(JSONObject data) throws InterruptedException {
		String content = data.toJSONString();
		byte[] bytes = content.getBytes(CharsetUtil.UTF_8);
		ByteBuf buf = Unpooled.copiedBuffer(bytes);
		
		DefaultFullHttpRequest request = new DefaultFullHttpRequest(
				HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath(),buf);
		request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
				.set(HttpHeaderNames.CONTENT_TYPE,HttpHeaderValues.APPLICATION_JSON)
				.set(HttpHeaderNames.ACCEPT_CHARSET,CharsetUtil.UTF_8)
				.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP)
				.set(HttpHeaderNames.CONTENT_LENGTH,bytes.length);
		
		Channel channel = this.getAndCreateChannel(hostname + ":" + port);
		// Send the HTTP request.
		channel.writeAndFlush(request).addListener((ChannelFutureListener) f -> log.info("send http request {}",f.isSuccess()));
	}
	
	public static void main(String[] args) {
		HttpClient httpClient = new HttpClient();
		try {
			httpClient.start();
			httpClient.sendSyncImpl(new JSONObject());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		;
	}
}
