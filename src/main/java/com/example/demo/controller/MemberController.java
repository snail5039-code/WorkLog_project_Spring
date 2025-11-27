package com.example.demo.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.Member;
import com.example.demo.service.MemberService;

import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "http://localhost:5174", allowCredentials = "true") //쿠키 설정
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
	public int login(@RequestBody Member loginData, HttpSession session) {
	    
	    Member member = this.memberService.getMemberLoginId(loginData);

	    if(member == null) {
	    	 String message = String.format("%s는 존재하지 않는 아이디 입니다.", loginData.getLoginId());
	    	 return 0;
	    }
	    
	    if(!member.getLoginPw().equals(loginData.getLoginPw())) {
	    	return 0;
	    }
	    
	    session.setAttribute("logindeMemberId", member.getId());
	    
	    return member.getId();
	}
	
	@GetMapping("/usr/member/session")
	public int addSession(HttpSession session) {
		// null 포인터 땜에 인티져로
		// 이게 지속적으로 상태 보는 거임!
		Integer isLoginedId = (Integer) session.getAttribute("logindeMemberId");
		
	    return isLoginedId != null ? isLoginedId : 0;
	}
	
	@PostMapping("/usr/member/logout")
	public int logout(HttpSession session) {
	    
		session.invalidate();
	    
	    return 0;
	}	
	
	@GetMapping("/usr/member/checkLoginId")
	public int checkLoginId(String loginId) {
		int isIdDupChek = this.memberService.checkLoginId(loginId);
		// 굳이 다른거 할 필요 없으니 인트로 반환 있으면 1, 없으면 0으로 그래서 쿼리 날릴때도 카운트로 함!
	    return isIdDupChek;
	}	
	
}
