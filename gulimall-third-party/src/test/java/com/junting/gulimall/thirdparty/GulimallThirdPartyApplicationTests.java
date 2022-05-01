package com.junting.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import com.junting.gulimall.thirdparty.utils.HttpUtils;
import org.apache.http.HttpResponse;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallThirdPartyApplicationTests {
	@Autowired
	OSSClient ossClient;
	@Test
	public void contextLoads() throws FileNotFoundException {
		InputStream inputStream = new FileInputStream("C:\\Users\\ZengJunting\\Desktop\\u=1316800099,2051258115&fm=26&fmt=auto.webp");
		ossClient.putObject("minizeng123", "axiba.jpg", inputStream);

		// 关闭OSSClient。
		ossClient.shutdown();

		System.out.println("保存成功");
	}

	@Test
	public void textCode() {
		String host = "https://gyytz.market.alicloudapi.com";
		String path = "/sms/smsSend";
		String method = "POST";
		String appcode = "dc8025881401414890810b4e19cc4dd6";
		Map<String, String> headers = new HashMap<String, String>();
		//最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
		headers.put("Authorization", "APPCODE " + appcode);
		Map<String, String> querys = new HashMap<String, String>();
		querys.put("mobile", "13538312448");
		querys.put("param", "**code**:12345,**minute**:5");
		querys.put("smsSignId", "2e65b1bb3d054466b82f0c9d125465e2");
		querys.put("templateId", "908e94ccf08b4476ba6c876d13f084ad");
		Map<String, String> bodys = new HashMap<String, String>();


		try {
			/**
			 * 重要提示如下:
			 * HttpUtils请从
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/src/main/java/com/aliyun/api/gateway/demo/util/HttpUtils.java
			 * 下载
			 *
			 * 相应的依赖请参照
			 * https://github.com/aliyun/api-gateway-demo-sign-java/blob/master/pom.xml
			 */
			HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
			System.out.println(response.toString());
			//获取response的body
			//System.out.println(EntityUtils.toString(response.getEntity()));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
