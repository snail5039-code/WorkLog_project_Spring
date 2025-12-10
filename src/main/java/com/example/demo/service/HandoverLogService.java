package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

import com.example.demo.dao.HandoverLogDao;
import com.example.demo.dto.HandoverLog;

@Service
public class HandoverLogService {
	
	private final HandoverLogDao handoverLogDao;
	
	public HandoverLogService(HandoverLogDao handoverLogDao) {
		this.handoverLogDao = handoverLogDao;
	}

	public List<HandoverLog> getMyHandoverLog(Integer memberId, int offset, int size) {
		return this.handoverLogDao.getMyHandoverLog(memberId, offset, size);
	}

	public int getMyHandoverLogCount(Integer memberId) {
		return this.handoverLogDao.getMyHandoverLogCount(memberId);
	}

	public void saveHandoverLog(Integer memberId, String name, String toName, String toJob, String fromJob,
			String title, LocalDate fromDate, LocalDate toDate, String content) {
		this.handoverLogDao.saveHandoverLog(memberId, title, name, toName, toJob, fromJob, fromDate, toDate, content);
	}

	public HandoverLog findById(int id) {
		return this.handoverLogDao.findById(id);
	}


}
