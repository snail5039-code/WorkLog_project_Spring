package com.example.demo.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class NeedLogoutInterceptor implements HandlerInterceptor{
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		HttpSession session = request.getSession(false);
		
		if(session != null && session.getAttribute("logindeMemberId") != null) {
			System.out.println("로그아웃 후 이용해주세요");
			
			return false;
		}
		return HandlerInterceptor.super.preHandle(request, response, handler);
	}
}
//혹시 모르니 남겨놓자.