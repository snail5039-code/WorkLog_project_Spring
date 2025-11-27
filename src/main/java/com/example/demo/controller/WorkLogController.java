package com.example.demo.controller;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dto.WorkLog;
import com.example.demo.service.FileAttachService;
import com.example.demo.service.WorkChatAIService;
import com.example.demo.service.WorkLogService;

import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "http://localhost:5173" , allowCredentials = "true")
@RequestMapping("/api")
public class WorkLogController {

    private final WorkChatAIService workChatAIService;

	
	private FileAttachService fileAttachService;
	private WorkLogService workLogService;
	// 의존성 주입
	public WorkLogController(WorkLogService workLogService, FileAttachService fileAttachService, WorkChatAIService workChatAIService) {
		this.workLogService = workLogService;
		this.fileAttachService = fileAttachService;
		this.workChatAIService = workChatAIService;
	}
	
	@PostMapping("/usr/work/workLog")  //MultipartFile 이거는 스프링부트 내장이라서 바로 사용 가능함, 리액트에서 multiple를 받아온거!
	public String writeWorkLog(String title, String mainContent, String sideContent, List<MultipartFile> files, HttpSession session) {
		String formatGuideline = getGuidelineAttachment(files); // 밑에서 만든거 가져온거임!
		
		// ai한테 보낼 데이터 준비 모든 거를 하나로 합치고 summaryContent 요거는 등록 후 상세보기할 때 나오는 공간임
		String fullContentForAI = "제목: " + title + "\n메인: " + mainContent + "\n비고: " + sideContent;
	    String summaryContent = "";
	    
	    // 매개변수로 넘겨서 서비스에서 요약 시키는거 그리고 되면 summaryContent 여기에 담음
	    try {
	    	summaryContent = workChatAIService.summarizeWorkLog(fullContentForAI, formatGuideline);
	    	System.out.println("요약 완료");
		} catch (Exception e) {
			System.err.println("오류 발생...");
			summaryContent = "요약 실패 내용 확인!";
		}
	    
	    // MultipartFile 이거는 따로 테이블 만들어서 보관해야됌!
		// 여기서 같이 넘김 요약 파일을 
		WorkLog workLogData = new WorkLog();
		workLogData.setTitle(title);
		workLogData.setMainContent(mainContent);
		workLogData.setSideContent(sideContent);
		workLogData.setSummaryContent(summaryContent);
		
		int memberIdObj = (int) session.getAttribute("logindeMemberId"); 
		
		this.workLogService.writeWorkLog(workLogData, memberIdObj);
		
		int workLogId = this.workLogService.getLastInsertId();
//		 첨부 파일 처리, 맨 위는 로그인 안된 상태에서 넘길때 방지
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
	
	private String getGuidelineAttachment(List<MultipartFile> files) {
		// 만약에 파일 양식이 없을 경우 기본 양식을 활용
		if(files == null || files.isEmpty()) {
			return "1. 오늘 목표:\n2. 주요 성과:\n3. 남은 일:";
		}
		// txt로 찾는 파일 찾기 
		for(MultipartFile file : files) {
			if(file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
				try {
					// 파일의 모든 데이터를 가져와서 우리말로 읽기 쉬운 문자열로 저장하삼 이라는 뜻 getBytes이거는 안에 내장된 함수임
					String content = new String(file.getBytes(), StandardCharsets.UTF_8);
					
					System.out.println("첨부파일 양식 추출 성공 ");
					
					return content.trim();
				} catch (Exception e) {
					System.err.println("첨부파일 읽기 실패");
					return "1. 오늘 목표:\n2. 주요 성과:\n3. 남은 일:";
				}
			}
		}
		return "1. 오늘 목표:\n2. 주요 성과:\n3. 남은 일:";
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
