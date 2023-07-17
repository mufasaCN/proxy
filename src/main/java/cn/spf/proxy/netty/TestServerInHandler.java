package cn.spf.proxy.netty;

import cn.spf.proxy.forest.HttpForestClient;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/7/1 17:35
 */
@Slf4j
public class TestServerInHandler extends ChannelInboundHandlerAdapter {
	
	private HttpForestClient httpForestClient;
	public TestServerInHandler(ApplicationContext applicationContext) {
		httpForestClient = applicationContext.getBean(HttpForestClient.class);
	}
	
	@Override
	public void channelRegistered(ChannelHandlerContext ctx) {
		log.info("测试服务处理器-->>注册，远程地址:{}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
	}
	
	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) {
		log.info("测试服务处理器-->>取消注册，远程地址:{}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		log.info("测试服务处理器-->>活跃的，远程地址:{}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		log.info("测试服务处理器-->>不活跃的，远程地址:{}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		log.info("测试服务处理器-->>读取，远程地址:{}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
		String content = (String) msg;
		content += "}";
		log.info(content);
		File file = new File("D:\\IPID美国10000.txt");
		if(file.isFile() && file.exists()) {
			InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
			BufferedReader br = new BufferedReader(isr);
			String lineTxt;
			int index = 0;
			long current = System.currentTimeMillis();
			while ((lineTxt = br.readLine()) != null) {
				
				String username = lineTxt.split("----")[1];
				String password = lineTxt.split("----")[2];
				return;
			}
		}
	}
	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		log.info("测试服务处理器-->>读取完毕，远程地址:{}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		log.error("测试服务处理器-->>异常，远程地址:{}", RemotingHelper.parseChannelRemoteAddr(ctx.channel()));
		log.error(cause.getMessage());
	}
}
