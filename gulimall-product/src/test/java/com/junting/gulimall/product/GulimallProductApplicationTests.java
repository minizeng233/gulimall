package com.junting.gulimall.product;


import com.aliyun.oss.OSSClient;
import com.junting.gulimall.product.service.BrandService;
import com.junting.gulimall.product.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.context.junit4.SpringRunner;
import org.junit.runner.RunWith;
import org.junit.Test;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallProductApplicationTests {
    @Autowired
    BrandService brandService;

    @Autowired
    OSSClient ossClient;

    @Test
    public void test() throws FileNotFoundException {
//        // Endpoint以杭州为例，其它Region请按实际情况填写。
//        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
//        // 云账号AccessKey有所有API访问权限，建议遵循阿里云安全最佳实践，创建并使用RAM子账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建。
//        String accessKeyId = "LTAI5tHQgyX7b3CTsMt6q4yY";
//        String accessKeySecret = "ofEpH6gUPqrQF6tIfI45JJHQoyLFWc";
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        // 上传文件流。
        InputStream inputStream = new FileInputStream("C:\\Users\\ZengJunting\\Desktop\\u=1316800099,2051258115&fm=26&fmt=auto.webp");
        ossClient.putObject("minizeng123", "meinv.jpg", inputStream);

		// 关闭OSSClient。
        ossClient.shutdown();

		System.out.println("保存成功");

    }

//    @Autowired
//    CategoryService categoryService;
//    @Test
//    public void testFindPath(){
//        Long[] paths = categoryService.findCategoryPath(335L);
//        log.info("查询总路径为{}", Arrays.asList(paths));
//    }


}
