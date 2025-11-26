package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dao.WorkLogDao;
import com.example.demo.dto.WorkLog;

@Service
public class WorkLogService {
	
	private WorkLogDao workLogDao;
	// 의존성 주입
	public WorkLogService(WorkLogDao workLogDao) {
		this.workLogDao = workLogDao;
	}
	
	public WorkLog writeWorkLog(WorkLog workLogData, int memberId) {
		return this.workLogDao.writeWorkLog(workLogData, memberId);
	}

	public List<WorkLog> showList() {
		return this.workLogDao.showList();
	}

	public WorkLog showDetail(int id) {
		return this.workLogDao.showDetail(id);
	}

	public int doModify(int id, WorkLog modifyData) {
		return this.workLogDao.doModify(id, modifyData);
	}
	
}
