package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.dto.Template;
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
					, documentType = #{workLogData.documentType}
					, memberId = #{memberId}               
					, boardId = 1                   
			""")
	public void writeWorkLog(@Param("workLogData") WorkLog workLogData, @Param("memberId") int memberId);

	@Select("""
			select w.*, m.loginId as writerName
				from workLog as w
				inner join member as m
				on w.memberId = m.id 
				order by id desc
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
	
	@Select("""
			select *
				from template
				where templateFileName = #{templateFileName}
			""")
	public List<Template> selectMappingsByFileName(String templateFileName);
	
	
	@Update("""
			UPDATE workLog
			     SET docxPath = #{docxPath}
			     WHERE id = #{id}
			""")
	public void updateDocxPath(@Param("id") int workLogId, @Param("docxPath") String savedPath);

	// ⭐ 여기에 이거 추가!
	@Select("""
	        SELECT *
	          FROM template
	         WHERE templateFileName LIKE CONCAT(#{pattern}, '%')
	        """)
	public List<Template> selectMappingsByFileNameLike(String pattern);
}
