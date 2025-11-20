package com.example.demo.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

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

}
