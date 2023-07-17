package cn.spf.proxy;

import com.dtflys.forest.springboot.annotation.ForestScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ForestScan(value = "cn.spf.proxy.forest")
public class ProxyApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(ProxyApplication.class, args);
	}
	
}
