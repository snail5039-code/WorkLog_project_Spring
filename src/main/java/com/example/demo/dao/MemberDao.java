package com.example.demo.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.dto.Member;

@Mapper
public interface MemberDao {
	
	@Insert("""
			insert into member
				set regDate = now()
					, updateDate = now()
					, loginId = #{loginId}
					, loginPw = #{loginPw}
					, name = #{name}
					, email = #{email}
					, sex = #{sex}
					, address = #{address}
			""")
	void memberJoin(Member memberJoin);
	
	@Select("""
			select *
				from member
				where loginId = #{loginId}
			""")
	Member getMemberLoginId(Member loginData);
	
	@Select("""
			select count(loginId)
				from member 
				where loginId = #{loginId}
			""")
	int checkLoginId(String loginId);
	
	@Select("""
			select *
				from member 
				where id = #{memberId}
			""")
	Member getMemberById(int memberId);
	
	@Update("""
			update member
				set updateDate = now()
				 	, loginPw = #{loginPw}
				 	, name = #{name}
				 	, email = #{email}
				 	, address = #{address}
		 		where id = #{id}
			""")
	int updateMyInfo(Member member);
	
	@Select("""
			select *
				from member
				where name = #{name}
					and email = #{email}
			""")
	Member findByNameAndEmail(String name, String email);
	
	@Update("""
			update member
				set loginPw = #{newPassword}
				where id = #{id}
			""")
	void changePassword(int id, String newPassword);
	
	@Select("""
			select *
				from member
				where loginId = #{loginId}
					and email = #{email}
			""")
	Member findByLoginIdAndEmail(String loginId, String email);
	
}