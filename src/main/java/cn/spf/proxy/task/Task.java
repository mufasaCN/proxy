package cn.spf.proxy.task;

import cn.spf.proxy.util.AddressUtil;
import io.netty.util.internal.StringUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/6/30 21:07
 */

@Component
@Slf4j
public class Task{
	
	public static LinkedBlockingQueue<String> queue = new LinkedBlockingQueue<>();
	
	@PostConstruct
	public void start() {
		Thread thread = new Thread(() -> {
			while (true){
				try {
					String hostname = queue.take();
					if (!StringUtil.isNullOrEmpty(hostname)){
						InetAddress[] inetadd = InetAddress.getAllByName(hostname);
						//遍历所有的ip并输出
						for (int i = 0; i < inetadd.length; i++) {
							System.out.println("ip：" + inetadd[i].getHostAddress()+",归属地:" + AddressUtil.getCityInfo(inetadd[i].getHostAddress()));
						}
					}
				} catch (InterruptedException | UnknownHostException e) {
					throw new RuntimeException(e);
				}
			}
		});
		thread.start();
	}
	
	
}
