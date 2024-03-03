package com.junting.gulimall.search;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallSearchApplicationTests {

	@Autowired
	RestHighLevelClient restHighLevelClient;

	@Test
	void contextLoads() {
	}

}
