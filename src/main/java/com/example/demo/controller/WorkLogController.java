package com.example.demo.controller;

import java.io.Console;
import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.FileAttachDao;
import com.example.demo.dto.WorkLog;
import com.example.demo.service.FileAttachService;
import com.example.demo.service.WorkLogService;

import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@RequestMapping("/api")
public class WorkLogController {

	
	private FileAttachService fileAttachService;
	private WorkLogService workLogService;
	// 의존성 주입
	public WorkLogController(WorkLogService workLogService, FileAttachDao fileAttachDao, FileAttachService fileAttachService) {
		this.workLogService = workLogService;
		this.fileAttachService = fileAttachService;
	}
	
	@PostMapping("/usr/work/workLog")  //MultipartFile 이거는 스프링부트 내장이라서 바로 사용 가능함, 리액트에서 multiple를 받아온거!
	public String writeWorkLog(String title, String mainContent, String sideContent, List<MultipartFile> files, HttpSession session) {
	    // MultipartFile 이거는 따로 테이블 만들어서 보관해야됌!
		// 나중에 바꾸기 ㅋㅋ 
		WorkLog workLogData = new WorkLog();
		
		workLogData.setTitle(title);
		workLogData.setMainContent(mainContent);
		workLogData.setSideContent(sideContent);
		System.out.println("세션 memberId = " + session.getAttribute("logindeMemberId"));
		int memberId =  (int) session.getAttribute("logindeMemberId");
		System.out.println("세션 memberId = " + session.getAttribute("logindeMemberId"));
		WorkLog saveWorkLog = this.workLogService.writeWorkLog(workLogData, memberId);
		
		int workLogId = saveWorkLog.getId();
		
		// 첨부 파일 처리, 맨 위는 로그인 안된 상태에서 넘길때 방지
		if(workLogId == 0) {
			System.out.println("저장 실패 파일 처리 건너뛰기를 실행");
		} else { // 밑에는 가져온 파일들의 값이 있을 때 순회를 돌려서 있는 파일만 골라서 넘기겠다라는 의미임
			if(files != null && !files.isEmpty()) {
				for(MultipartFile file : files) {
					if(!file.isEmpty()) {
						this.fileAttachService.fileInsert(workLogId, file);
					}
				}
			}
		}
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
