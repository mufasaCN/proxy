package cn.spf.proxy.controller;

import cn.spf.proxy.ip2region.Ip;
import cn.spf.proxy.util.AddressUtil;
import cn.spf.proxy.util.IPUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/6/30 20:36
 */
@RestController
@RequestMapping(value = "/proxy")
@Slf4j
public class ProxyController {
	
	@RequestMapping(value = "/address")
//	@Ip
	public String realIp(HttpServletRequest request){
		String ip = IPUtil.getIpAddr(request);
		String cityInfo = AddressUtil.getCityInfo(ip);
		String address = MessageFormat.format("当前IP为:[{0}]；当前IP地址解析出来的地址为:[{1}]", ip, cityInfo);
		log.info(address);
		return address;
	}
}
