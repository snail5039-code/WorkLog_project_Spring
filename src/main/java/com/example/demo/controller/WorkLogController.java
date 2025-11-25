package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.WorkLog;
import com.example.demo.service.WorkLogService;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api")
public class WorkLogController {
	
	private WorkLogService workLogService;
	// 의존성 주입
	public WorkLogController(WorkLogService workLogService) {
		this.workLogService = workLogService;
	}
	
	@PostMapping("/usr/work/workLog")
	public String writeWorkLog(@RequestBody WorkLog workLogData ) {
	    
		this.workLogService.writeWorkLog(workLogData);
		
		
		return "데이터 입력 완료";
	}
	
	@GetMapping("/usr/work/list")
	public List<WorkLog> showList() {
		return this.workLogService.showList();
	}
	
	@GetMapping("/usr/work/detail/{id}")
	public WorkLog showDetail(@PathVariable("id") int id) {
		return this.workLogService.showDetail(id);
	}
	
	@PostMapping("/usr/work/modify/{id}")
	public int modify(@PathVariable("id") int id, @RequestBody WorkLog modifyData) {
		return this.workLogService.doModify(id, modifyData);
	}
}
