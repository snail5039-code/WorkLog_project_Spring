package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Member;
import com.example.demo.service.MemberService;

@RestController
public class MemberController {
	
	private MemberService memberService;
	// 의존성 주입
	public MemberController(MemberService memberService) {
		this.memberService = memberService;
	}
	
	@PostMapping("/usr/member/join")
	public String join(@RequestBody Member memberJoin ) {
		System.out.println("--- 리액트 요청 데이터 ---");
	    System.out.println("Title: " + memberJoin);
	    System.out.println("--------------------------");
	    
	    this.memberService.memberJoin(memberJoin);
		return "데이터 입력 완료";
	}
}
