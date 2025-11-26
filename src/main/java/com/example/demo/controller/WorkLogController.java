package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
	
	@PostMapping("/usr/work/workLog")  //MultipartFile 이거는 스프링부트 내장이라서 바로 사용 가능함, 리액트에서 multiple를 받아온거!
	public String writeWorkLog(String title, String mainContent, String sideContent, List<MultipartFile> files) {
	    // MultipartFile 이거는 따로 테이블 만들어서 보관해야됌!
		// 나중에 바꾸기 ㅋㅋ 
		WorkLog workLogData = new WorkLog();
		
		workLogData.setTitle(title);
		workLogData.setMainContent(mainContent);
		workLogData.setSideContent(sideContent);
		
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
