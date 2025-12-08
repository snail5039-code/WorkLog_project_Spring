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
	public int checkLoginId(String loginId) {
		return this.memberDao.checkLoginId(loginId);
	}
	public Member getMemberById(int memberId) {
		return this.memberDao.getMemberById(memberId);
	}
	public int updateMyInfo(Member member) {
		return this.memberDao.updateMyInfo(member);
	}
	public Member findByNameAndEmail(String name, String email) {
		return this.memberDao.findByNameAndEmail(name, email);
	}
	public void changePassword(int id, String newPassword) {
		this.memberDao.changePassword(id, newPassword);
	}
	public Member findByLoginIdAndEmail(String loginId, String email) {
		return this.memberDao.findByLoginIdAndEmail(loginId, email);
	}
	
}