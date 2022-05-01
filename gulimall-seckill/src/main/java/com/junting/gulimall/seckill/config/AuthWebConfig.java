package com.junting.gulimall.seckill.config;


import com.junting.gulimall.seckill.interceptor.LoginUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

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
	LoginUserInterceptor loginUserInterceptor;
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(loginUserInterceptor).addPathPatterns("/**");
	}
}
