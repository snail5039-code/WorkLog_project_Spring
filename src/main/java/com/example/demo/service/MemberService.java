package com.example.demo.service;

import org.springframework.stereotype.Service;

import com.example.demo.dao.MemberDao;
import com.example.demo.dto.Member;

@Service
public class MemberService {
	
	private MemberDao memberDao;
	// 의존성 주입
	public MemberService(MemberDao memberDao) {
		this.memberDao = memberDao;
	}
	public void memberJoin(Member memberJoin) {
		this.memberDao.memberJoin(memberJoin);
	}
	public Member getMemberLoginId(Member loginData) {
		return this.memberDao.getMemberLoginId(loginData);
	}
	
}
