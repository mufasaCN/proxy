package cn.spf.proxy.forest;

import com.dtflys.forest.annotation.Get;
import com.dtflys.forest.annotation.HTTPProxy;
import com.dtflys.forest.annotation.Post;
import com.dtflys.forest.annotation.Var;
import com.dtflys.forest.callback.OnError;
import com.dtflys.forest.callback.OnSuccess;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.springframework.stereotype.Service;

/**
 * 作者：spf
 * 描述：
 * 创建时间：2023/7/1 8:56
 */
@Service
public interface HttpForestClient {
	@Get(value = "https://echo.apifox.com/anything",headers = {"User-Agent:Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)"})
	@HTTPProxy(
			host = "49.51.67.180",
			port = "2333",
			username = "{username}",
			password = "{password}",
			headers = {"User-Agent:Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)",
					"Accept:*/*",
					"Connection","keep-alive"
			}
	)
	String postAddressWithProxy(@Var("username") String username, @Var("password")String password, OnSuccess<String> onSuccess, OnError onError);
	
}
