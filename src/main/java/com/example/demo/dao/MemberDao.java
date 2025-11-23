package com.example.demo.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

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
	
}
