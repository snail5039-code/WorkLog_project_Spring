package com.example.demo.dao;

import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.dto.HandoverLog;

@Mapper
public interface HandoverLogDao {
	
	@Select("""
			select *
				from handoverLog
				where memberId = #{memberId}
				order by id desc
				limit #{size} offset #{offset}
			""")
	List<HandoverLog> getMyHandoverLog(Integer memberId, int offset, int size);
	
	@Select("""
			select count(*)
				from handoverLog
				where memberId = #{memberId}
			""")
	int getMyHandoverLogCount(Integer memberId);

	@Insert("""
			insert into handoverLog
				set regDate = now()
					, updateDate = now()
					, memberId = #{memberId}
					, title = #{title}
					, writerName = #{name}
					, toName = #{toName}
					, toJob = #{toJob}
					, fromJob = #{fromJob}
					, fromDate = #{fromDate}
					, toDate = #{toDate}
					, content = #{content}
			""")
	void saveHandoverLog(Integer memberId, String name, String toName, String toJob, String fromJob,
			String title, LocalDate fromDate, LocalDate toDate, String content);
	
	@Select("""
			select *
				from handoverLog
				where id = #{id}
			""")
	HandoverLog findById(int id);
}
