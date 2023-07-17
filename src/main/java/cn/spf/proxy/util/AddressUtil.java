package cn.spf.proxy.util;

import org.apache.commons.io.FileUtils;
import org.lionsoul.ip2region.xdb.Searcher;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/6/30 20:30
 */
public class AddressUtil {
	/**
	 * 当前记录地址的本地DB
	 */
	private static final String TEMP_FILE_DIR = "/home/admin/app/";
	
	/**
	 * 根据IP地址查询登录来源
	 *
	 * @param ip
	 * @return 国家|区域|省份|城市|ISP
	 */
	public synchronized static String getCityInfo(String ip) {
		try {
			// 获取当前记录地址位置的文件
			String dbPath = Objects.requireNonNull(AddressUtil.class.getResource("/ip2region/ip2region.xdb")).getPath();
			File file = new File(dbPath);
			//如果当前文件不存在，则从缓存中复制一份
			if (!file.exists()) {
				dbPath =    TEMP_FILE_DIR + "ip.db";
				System.out.println(MessageFormat.format("当前目录为:[{0}]", dbPath));
				file = new File(dbPath);
				FileUtils.copyInputStreamToFile(Objects.requireNonNull(AddressUtil.class.getClassLoader().getResourceAsStream("classpath:ip2region/ip2region.xdb")), file);
			}
			//创建查询对象
			Searcher searcher = Searcher.newWithFileOnly(dbPath);
			//开始查询
			return searcher.search(ip);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//默认返回空字符串
		return "";
	}
	
	/**
	 * 同步
	 * 根据域名获取ip
	 * @param hostname
	 * @return
	 */
	public synchronized static List<String> getIp(String hostname) {
		List<String> ips = new ArrayList<>();
		try {
			InetAddress[] inetadd = InetAddress.getAllByName(hostname);
			//遍历所有的ip并输出
			for (int i = 0; i < inetadd.length; i++) {
				ips.add(inetadd[i].getHostAddress());
			}
		} catch (UnknownHostException e) {
			throw new RuntimeException(e);
		}
		return ips;
	}
	public static void main(String[] args) {
		System.out.println(getCityInfo("1.2.3.4"));
	}
}
