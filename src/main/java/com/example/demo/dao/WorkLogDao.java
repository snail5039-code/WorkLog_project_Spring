package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.dto.WorkLog;

@Mapper
public interface WorkLogDao {
// 우선 맴버, 보드는 안만들어서 하드 코딩중
	@Insert("""
			insert into workLog
				set regDate = now()
					, updateDate = now()
					, title = #{title}
					, mainContent = #{mainContent}
					, sideContent = #{sideContent}
					, memberId = 1                
					, boardId = 1                   
			""")
	public void writeWorkLog(WorkLog workLogData);

	@Select("""
			select w.*, m.loginId as writerName
				from workLog as w
				inner join member as m
				on w.memberId = m.id 
			""")
	public List<WorkLog> showList();

	@Select("""
			select w.*, m.loginId as writerName
				from workLog as w
				inner join member as m
				on w.memberId = m.id 
				where w.id = #{id}
			""")
	public WorkLog showDetail(int id);

}
