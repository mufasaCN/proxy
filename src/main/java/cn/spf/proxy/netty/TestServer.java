package cn.spf.proxy.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/6/30 17:30
 */
@Component
@Slf4j
public class TestServer implements ApplicationContextAware {
	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();
	private Channel channel;
	
	@Value("${netty.server.testServer.hostname}")
	private String hostname;
	@Value("${netty.server.testServer.port}")
	private int port;
	
	private ApplicationContext applicationContext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	@PostConstruct
	public ChannelFuture start() {
		ChannelFuture f = null;
		try {
			//ServerBootstrap负责初始化netty服务器，并且开始监听端口的socket请求
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.localAddress(new InetSocketAddress(hostname,port))
					.option(ChannelOption.SO_BACKLOG,1024)
					.option(ChannelOption.SO_REUSEADDR, true)
					.option(ChannelOption.SO_KEEPALIVE, true)
					.childOption(ChannelOption.TCP_NODELAY, true)
					.option(ChannelOption.SO_SNDBUF, 65535)
					.option(ChannelOption.SO_RCVBUF, 65535)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						protected void initChannel(SocketChannel socketChannel) throws Exception {
							byte[] END_DELIMITER = "}".getBytes(CharsetUtil.UTF_8);
							ByteBuf delimiter = Unpooled.copiedBuffer(END_DELIMITER);
							//为监听客户端read/write事件的Channel添加用户自定义的ChannelHandler
							socketChannel.pipeline()
									.addLast(
//											new DelimiterBasedFrameDecoder(1024, delimiter),
											new StringDecoder(CharsetUtil.UTF_8),
											new StringEncoder(CharsetUtil.UTF_8));
											new TestServerInHandler(applicationContext);
						}
					});
			f = b.bind().sync();
			channel = f.channel();
			log.info("测试服务-->>启动成功。");
		} catch (Exception e) {
			log.error("测试服务-->>启动异常。",e.getMessage());
		} finally {
			if (f != null && f.isSuccess()) {
				log.info("测试服务-->>监听地址：" + hostname + " 端口 " + port + "，等待连接...");
			} else {
				log.error("测试服务-->>启动失败。");
			}
		}
		return f;
	}
	/**
	 * 停止服务
	 */
	@PreDestroy
	public void destroy() {
		log.info("测试服务关闭中...");
		if(channel != null) { channel.close();}
		workerGroup.shutdownGracefully();
		bossGroup.shutdownGracefully();
		log.info("测试服务关闭成功!");
	}
	
	
}
