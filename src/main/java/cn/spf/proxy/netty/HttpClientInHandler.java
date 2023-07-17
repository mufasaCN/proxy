package cn.spf.proxy.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/7/17 15:43
 */
@Slf4j
public class HttpClientInHandler extends SimpleChannelInboundHandler<HttpObject> {
	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, HttpObject msg) throws Exception {
		if (msg instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) msg;
			log.info("status: " + response.status());
			log.info("VERSION: " + response.protocolVersion());
			if (!response.headers().isEmpty()){
				for (String name: response.headers().names()){
					for (String value: response.headers().getAll(name)){
						log.info("HEADER:" + name + "=" + value);
					}
				}
				System.out.println();
			}
			if (msg instanceof HttpContent) {
				HttpContent content = (HttpContent) msg;
				String returnContent = content.content().toString(CharsetUtil.UTF_8);
				if (StringUtils.isEmpty(returnContent)) {
					log.error("get response content is empty");
					return;
				}
				log.info("接收到数据1：" + returnContent);
			}
		}
	}
}
