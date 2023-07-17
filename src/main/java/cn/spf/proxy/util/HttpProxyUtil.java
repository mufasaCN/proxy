package cn.spf.proxy.util;

import io.netty.util.CharsetUtil;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/7/1 22:35
 */
public class HttpProxyUtil {
	public static void main(String[] args) throws Exception {
		URL url = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
		
		// 构建传入代理连接的ip、port和用户名密码
		List<String> authList = new ArrayList<>();
		String hostname="49.51.67.180";
		int  port = 2333;
		String authUser = "tuiguang3-zone-sto316761-region-us-session-123719kgk";
		String authPassword = "tuiguang3";
		
		//通过代理进行连接
		URLConnection urlConnection = url.openConnection(getProxy(hostname,port,authUser,authPassword));
		
		//构建input输入流
		InputStream input = urlConnection.getInputStream();
		
		//将内容打印至控制台
		System.out.println(getContent(input));
	}
	
	
	/**
	 * 设置Proxy并返回
	 * @param hostname
	 * @param port
	 * @param authUser
	 * @param authPassword
	 * @return
	 */
	public static Proxy getProxy(String hostname, Integer port ,String authUser,String authPassword){
		
		//设置认证信息
		setAuthProperties(authUser,authPassword);
		
		//构造proxy的地址和端口并返回
		SocketAddress socketAddress = new InetSocketAddress(hostname,port);
		Proxy proxy = new Proxy(Proxy.Type.HTTP,socketAddress);
		return proxy;
	}
	
	/**
	 * 设置认证相关信息
	 * @param authUser
	 * @param authPassword
	 */
	private static void setAuthProperties(String authUser, String authPassword) {
		System.setProperty("http.proxyUser", authUser);
		System.setProperty("http.proxyPassword", authPassword);
//		System.setProperty("jdk.http.auth.tunneling.disabledSchemes", "");
		Authenticator.setDefault(
				new Authenticator() {
					@Override
					public PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(authUser, authPassword.toCharArray());
					}
				}
		);
	}
	
	//获取网页的内容，通过StringBuilder将bytes转化为char
	public static String getContent (InputStream input) throws IOException {
		String content;
		int n;
		StringBuilder sb = new StringBuilder();
		while ((n = input.read()) != -1) {
			sb.append((char) n);
		}
		content = sb.toString();
		return content;
	}
}
