package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.demo.interceptor.NeedLoginInterceptor;
import com.example.demo.interceptor.NeedLogoutInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {
	
	private NeedLoginInterceptor needLoginInterceptor;
	private NeedLogoutInterceptor needLogoutInterceptor;
	
	public WebConfig(NeedLoginInterceptor needLoginInterceptor, NeedLogoutInterceptor needLogoutInterceptor) {
		this.needLoginInterceptor = needLoginInterceptor;
		this.needLogoutInterceptor = needLogoutInterceptor;
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/api/**").allowCredentials(true).allowedOrigins("http://localhost:3000")
		.allowedOrigins("http://localhost:5173");
	}
	//혹시 모르니 남겨놓자.
//	public void addInterceptors(InterceptorRegistry registry) {
//		registry.addInterceptor(needLoginInterceptor).addPathPatterns("/api/usr/work/workLog").addPathPatterns("/api/usr/work/list").addPathPatterns("/api/usr/member/logout");
//		
//		registry.addInterceptor(needLogoutInterceptor).addPathPatterns("/api/usr/member/login").addPathPatterns("/api/usr/member/join");
//	}
}
