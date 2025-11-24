package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Member;
import com.example.demo.service.MemberService;

import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") //쿠키 설정
@RequestMapping("/api")
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
	
	@PostMapping("/usr/member/login")
	public ResponseEntity<String> login(@RequestBody Member loginData, HttpSession session) {
	    
	    Member member = this.memberService.getMemberLoginId(loginData);

	    if(member == null) {
	    	 String message = String.format("%s는 존재하지 않는 아이디 입니다.", loginData.getLoginId());
	    	 return new ResponseEntity<>(message, HttpStatus.UNAUTHORIZED);
	    }
	    
	    if(!member.getLoginPw().equals(loginData.getLoginPw())) {
	    	return new ResponseEntity<>("비밀번호 틀림", HttpStatus.UNAUTHORIZED);
	    }
	    
	    session.setAttribute("logindeMemberId", member.getId());
	    
	    return new ResponseEntity<>("환영", HttpStatus.OK);
	}
	
	@PostMapping("/usr/member/logout")
	public ResponseEntity<String> logout(HttpSession session) {
	    
		session.invalidate();
	    
	    return new ResponseEntity<>("로그아웃 성공함", HttpStatus.OK);
	}	
	
}
