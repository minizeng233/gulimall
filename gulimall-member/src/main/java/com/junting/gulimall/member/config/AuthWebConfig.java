package com.junting.gulimall.member.config;


import com.junting.gulimall.member.interceptor.OrderInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
    *@Description	需要配置拦截器验证登录，购物车模块需要
    *@author mini_zeng
    *@Date 2022/1/15
    *@Param null
    *@return
    **/

@Configuration
public class AuthWebConfig implements WebMvcConfigurer {
	@Autowired
	OrderInterceptor orderInterceptor;
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(orderInterceptor).addPathPatterns("/**").excludePathPatterns(Arrays.asList("/login","/member/member/login","/member/member/oauth2/login"));
	}
}
