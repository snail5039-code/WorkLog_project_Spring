package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.dto.WorkLog;

@Mapper
public interface WorkLogDao {
// 우선 맴버, 보드는 안만들어서 하드 코딩중
	@Insert("""
			insert into workLog
				set regDate = now()
					, updateDate = now()
					, title = #{workLogData.title}
					, mainContent = #{workLogData.mainContent}
					, sideContent = #{workLogData.sideContent}
					, summaryContent = #{workLogData.summaryContent}
					, memberId = #{memberId}               
					, boardId = 1                   
			""")
	public void writeWorkLog(@Param("workLogData") WorkLog workLogData, @Param("memberId") int memberId);

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
	
	@Update("""
			update workLog
				set updateDate = now()
					, title = #{modifyData.title}
					, mainContent = #{modifyData.mainContent}
					, sideContent = #{modifyData.sideContent}
				where id = #{id}
			""")
	public int doModify(@Param("id") int id, @Param("modifyData") WorkLog modifyData);

	@Select("SELECT LAST_INSERT_ID()")
	public int getLastInsertId();

}
