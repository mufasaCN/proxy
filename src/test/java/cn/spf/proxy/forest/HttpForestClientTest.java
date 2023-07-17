package cn.spf.proxy.forest;

import com.dtflys.forest.exceptions.ForestRuntimeException;
import com.dtflys.forest.http.ForestRequest;
import com.dtflys.forest.http.ForestResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class HttpForestClientTest {
	HttpForestClient httpForestClient;
	@Autowired
	public HttpForestClientTest(HttpForestClient httpForestClient){
		this.httpForestClient = httpForestClient;
	}
	
	@Test
	void postAddressWithProxy() throws IOException {
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
				httpForestClient.postAddressWithProxy(username,password, (String outputContent, ForestRequest request, ForestResponse response) ->{
					log.info(outputContent);
				},(ForestRuntimeException ex, ForestRequest request, ForestResponse response) -> {
					// Todo 异常回调
					String content = response.getContent();
					log.error(ex.getMessage());
					int a = 0;
				});
				return;
			}
		}
	}
}