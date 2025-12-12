package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.dto.RePly;

@Mapper
public interface WorkReplyDao {
	// 등록
	@Insert("""
			insert into rePly
				set regDate = now()
					, updateDate = now()
					, workLogId = #{workLogId}
					, memberId = #{memberId}
					, content = #{content}
			""")
	public void addReply(@Param("memberId") int memberId, @Param("workLogId") int workLogId, @Param("content") String content);
	
	// 특정 글 댓글 조회
	@Select("""
			select r.*, m.loginId as writerName
				from rePly as r
				inner join member as m
				on r.memberId = m.id
				where r.workLogId = #{workLogId}
				order by r.id asc
			""")
	public List<RePly> getRepliesByWorkLogId(int workLogId);
	
	// 단일 댓글 조회
	@Select("""
			select r.*, m.loginId as writerName
				from rePly as r
				inner join member as m
				on r.memberId = m.id
				where r.id = #{replyId}
			""")
	public RePly findById(int replyId);
	
	@Delete("""
			delete from rePly
				where id = #{replyId}
			""")
	public void deleteById(int replyId);
	
	@Update("""
			update rePly
				set content = #{content}
					, updateDate = now()
				where id = #{replyId}	
			""")
	public void updateReply(@Param("replyId") int reply, @Param("content") String content);
	

}
