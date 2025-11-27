package com.example.demo.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class NeedLoginInterceptor implements HandlerInterceptor{
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		// 굳이 없으면 객체 생성 안하려고 하는 것
		HttpSession session = request.getSession(false);
		
		if(session == null || session.getAttribute("logindeMemberId") == null) {
			System.out.println("로그인되지 않은 접근 차단 데스!");
			return false;
		}
		
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
}
// 혹시 모르니 남겨놓자.