package com.junting.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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

}
