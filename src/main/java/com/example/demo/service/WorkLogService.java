package com.example.demo.service;

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
	
	public void writeWorkLog(WorkLog workLogData) {
		this.workLogDao.writeWorkLog(workLogData);
	}
	
}
