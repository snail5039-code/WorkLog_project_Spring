package com.example.demo.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.WorkLog;
import com.example.demo.service.WorkLogService;

@RestController
public class WorkLogController {
	
	private WorkLogService workLogService;
	// 의존성 주입
	public WorkLogController(WorkLogService workLogService) {
		this.workLogService = workLogService;
	}
	
	@PostMapping("/usr/work/workLog")
	public String writeWorkLog(@RequestBody WorkLog workLogData ) {
		System.out.println("--- 리액트 요청 데이터 ---");
	    System.out.println("Title: " + workLogData.getTitle());
	    System.out.println("MainContent: " + workLogData.getMainContent());
	    System.out.println("sideContent: " + workLogData.getSideContent());
	    System.out.println("--------------------------");
	    
		this.workLogService.writeWorkLog(workLogData);
		
		//System.out.println("데이터 잘 들어옴 "); 테스트
		
		return "데이터 입력 완료";
	}
}
