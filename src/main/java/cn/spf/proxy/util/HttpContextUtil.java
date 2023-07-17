package cn.spf.proxy.util;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

/**
 * 作者：spf
 * 描述：全局获取HttpServletRequest、HttpServletResponse
 * 创建时间：2023/6/30 20:30
 */
public class HttpContextUtil {
	private HttpContextUtil() {
	
	}
	
	public static HttpServletRequest getHttpServletRequest() {
		return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
	}
	
	public static HttpServletResponse getHttpServletResponse() {
		return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getResponse();
	}
}
