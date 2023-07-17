package cn.spf.proxy.ip2region;

import cn.spf.proxy.util.AddressUtil;
import cn.spf.proxy.util.HttpContextUtil;
import cn.spf.proxy.util.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/6/30 20:27
 */
//@Aspect
//@Component
@Slf4j
public class IpAspect {
	@Pointcut("@annotation(cn.spf.proxy.ip2region.Ip)")
	public void pointcut() {
		// do nothing
	}
	
	@Around("pointcut()")
	public Object doAround(ProceedingJoinPoint point) throws Throwable {
		HttpServletRequest request = HttpContextUtil.getHttpServletRequest();
		String ip = IPUtil.getIpAddr(request);
		String cityInfo = AddressUtil.getCityInfo(ip);
		log.info(MessageFormat.format("当前IP为:[{0}]；当前IP地址解析出来的地址为:[{1}]", ip, cityInfo));
		return point.proceed();
	}
}
