package cn.spf.proxy.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class PostClientUtil {
	public static String sendPost(String url,String param){
		OutputStreamWriter out =null;
		BufferedReader reader = null;
		String response = "";
		
		//创建连接
		try {
			URL httpUrl = null; //HTTP URL类 用这个类来创建连接
			//创建URL
			httpUrl = new URL(url);
			//建立连接
			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
//            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("User-Agent", "Mozilla/4.76");
			conn.setUseCaches(false);//设置不要缓存
			conn.setInstanceFollowRedirects(true);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.connect();
			//POST请求
			out = new OutputStreamWriter(
					conn.getOutputStream());
			out.write(param);
			out.flush();
			//读取响应
			reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			String lines;
			while ((lines = reader.readLine()) != null) {
				lines = new String(lines.getBytes(), "utf-8");
				response+=lines;
			}
			reader.close();
			// 断开连接
			conn.disconnect();
			
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！"+e);
			e.printStackTrace();
		}
		//使用finally块来关闭输出流、输入流
		finally{
			try{
				if(out!=null){
					out.close();
				}
				if(reader!=null){
					reader.close();
				}
			}
			catch(IOException ex){
				ex.printStackTrace();
			}
		}
		
		return response;
	}
}
