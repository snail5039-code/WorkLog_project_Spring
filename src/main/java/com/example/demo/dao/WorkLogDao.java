package com.example.demo.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.dto.TemplateUsageDto;
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
					, templateId = #{workLogData.templateId}              
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
				where boardId = #{boardId}
				order by id desc
			""")
	public List<WorkLog> showListByBoardId(Integer boardId);

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
			select count(*)
				from workLog
				where memberId = #{memberId}
			""")
	public int getMyWorkLogsCount(int memberId);
	// 카운트 안쓰면 터짐!
	@Select("""
			select count(*)
				from workLog
				where memberId = #{memberId}
				and date_format(regDate, '%y-%m') = date_format(now(), '%y-%m')
			""")
	public int getThisMonthCount(int memberId);
	
	@Select("""
			select max(regDate)
				from workLog
				where memberId = #{memberId}
			""")
	public LocalDateTime getLastWrittenDate(int memberId);
	
	@Select("""
			select templateId, count(*) as count
				from workLog
				where memberId = #{memberId}
				group by templateId
				order by count(*) desc
				limit 3 
			""")
	public List<TemplateUsageDto> getTopTemplates(int memberId);
	
	@Select("""
			select *
				from workLog
				where memberId = #{memberId}
				order by id desc
				limit #{size} offset #{offset}
			""")
	public List<WorkLog> getMyWorkLogsPaged(int memberId, int offset, int size);
	
	@Select("""
	        select w.*, m.loginId as writerName
				 from workLog as w
			     inner join member as m
			     on w.memberId = m.id
			     order by w.id desc
			     limit #{size} offset #{offset}
	        """)
	public List<WorkLog> getBoardListPagedAll(int offset, int size);
	
	@Select("""
	        select w.*, m.loginId as writerName
	        	 from workLog as w
				 inner join member as m
			     on w.memberId = m.id
			     where w.boardId = #{boardId}
			     order by w.id desc
			     limit #{size} offset #{offset}
	        """)
	public List<WorkLog> getBoardListPagedByBoard(Integer boardId, int offset, int size);

	@Select("""
	        select count(*)
	        	from workLog
	        """)
	public int getBoardListCountAll();
	
	@Select("""
	        select count(*)
	        	from workLog
	        	where boardId = #{boardId}
	        """)
	public int getBoardListCountByBoard(Integer boardId);
	
	@Select("""
			select * 
				from workLog
				where memberId = #{memberId}
					and date(regDate) between #{s} and #{e}
				order by regDate asc
			""")
	public List<WorkLog> getLogsByDateRange(int memberId, LocalDate s, LocalDate e);


}