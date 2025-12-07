package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dao.WorkLogDao;
import com.example.demo.dto.FileAttach;
import com.example.demo.dto.TemplateUsageDto;
import com.example.demo.dto.WorkLog;

@Service
public class WorkLogService {
	
	private WorkLogDao workLogDao;
	private FileAttachService fileAttachService;
	// 의존성 주입
	public WorkLogService(WorkLogDao workLogDao, FileAttachService fileAttachService) {
		this.workLogDao = workLogDao;
		this.fileAttachService = fileAttachService;
	}
	
	public void writeWorkLog(WorkLog workLogData, int memberId) {
		this.workLogDao.writeWorkLog(workLogData, memberId);
	}

	public List<WorkLog> showList() {
		return this.workLogDao.showList();
	}
	
	public List<WorkLog> showListByBoardId(Integer boardId) {
		return this.workLogDao.showListByBoardId(boardId);
	}

	public WorkLog showDetail(int id) {
		WorkLog workLog = this.workLogDao.showDetail(id); 
	    
	    if (workLog != null) {
	        List<FileAttach> fileAttaches = fileAttachService.getFilesByWorkLogId(id);
	        workLog.setFileAttaches(fileAttaches);  // 그래서 요거 worklog에 만들어줬음 리스트로 받을 수 있게!
	    }
	    
	    return workLog;
	}

	public int doModify(int id, WorkLog modifyData) {
		return this.workLogDao.doModify(id, modifyData);
	}

	public int getLastInsertId() {
		return this.workLogDao.getLastInsertId();
	}

	public int getMyWorkLogsCount(int memberId) {
		return this.workLogDao.getMyWorkLogsCount(memberId);
	}

	public int getThisMonthCount(int memberId) {
		return this.workLogDao.getThisMonthCount(memberId);
	}

	public LocalDateTime getLastWrittenDate(int memberId) {
		return this.workLogDao.getLastWrittenDate(memberId);
	}

	public List<TemplateUsageDto> getTopTemplates(int memberId) {
		return this.workLogDao.getTopTemplates(memberId);
	}

	public List<WorkLog> getMyWorkLogsPaged(int memberId, int page, int size) {
		int offset = (page - 1) * size; 
		return workLogDao.getMyWorkLogsPaged(memberId, offset, size);
	}

	public List<WorkLog> getBoardListPaged(Integer boardId, int page, int size) {
		int offset = (page - 1) * size;
		if(boardId == null || boardId == 0) {
			 return workLogDao.getBoardListPagedAll(offset, size);
	    } else {
	        // 특정 게시판만
	        return workLogDao.getBoardListPagedByBoard(boardId, offset, size);
	    }
	}

	public int getBoardListCount(Integer boardId) {
		if (boardId == null || boardId == 0) {
	        return workLogDao.getBoardListCountAll();      // 전체 카운트
	    } else {
	        return workLogDao.getBoardListCountByBoard(boardId); // 해당 boardId 카운트
	    }
	}

	
}