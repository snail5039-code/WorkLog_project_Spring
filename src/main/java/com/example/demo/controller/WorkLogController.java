package com.example.demo.controller;

import java.util.List;

import org.apache.tika.Tika;
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
import lombok.extern.slf4j.Slf4j;

@Slf4j // 로킹 어노테이션
@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api")
public class WorkLogController {

	private final WorkChatAIService workChatAIService;

	private FileAttachService fileAttachService;
	private WorkLogService workLogService;
	// 문서 내용 추출 tika 라이브러리 추가 및 객체 생성
	private Tika tika = new Tika();

	// 의존성 주입
	public WorkLogController(WorkLogService workLogService, FileAttachService fileAttachService,
			WorkChatAIService workChatAIService) {
		this.workLogService = workLogService;
		this.fileAttachService = fileAttachService;
		this.workChatAIService = workChatAIService;
	}

	@PostMapping("/usr/work/workLog") // MultipartFile 이거는 스프링부트 내장이라서 바로 사용 가능함, 리액트에서 multiple를 받아온거!
	public String writeWorkLog(String title, String mainContent, String sideContent, List<MultipartFile> files,
			HttpSession session) {
		int memberIdObj = (int) session.getAttribute("logindeMemberId");

		String formatGuideline = getGuideContent(files); // 밑에서 있나 없나 확인하고 가져온거임!

		// ai한테 보낼 데이터 준비 모든 거를 하나로 합치고 summaryContent 요거는 등록 후 상세보기할 때 나오는 공간임
		StringBuilder fullContentBuilder = new StringBuilder(); // 모든 문자열 합치려고 하는것!
		fullContentBuilder.append("제목: ").append(title).append("\n").append("메인: ").append(mainContent).append("\n")
				.append("비고: ").append(sideContent).append("\n");
		
		// 첨부파일 양식하고 입력한 내용 하고 합치는 거임!
		fullContentBuilder.append(allFileContent(files));
		
		// Ai한테 넘길 준비 
		String fullContentForAI = fullContentBuilder.toString();
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

		this.workLogService.writeWorkLog(workLogData, memberIdObj);

		int workLogId = this.workLogService.getLastInsertId();
//		 첨부 파일 처리, 맨 위는 로그인 안된 상태에서 넘길때 방지
		if (workLogId == 0) {
			System.out.println("저장 실패 파일 처리 건너뛰기를 실행");
		} else { // 밑에는 가져온 파일들의 값이 있을 때 순회를 돌려서 있는 파일만 골라서 넘기겠다라는 의미임
			if (files != null && !files.isEmpty()) {
				for (MultipartFile file : files) {
					if (!file.isEmpty()) {
						this.fileAttachService.fileInsert(workLogId, file);
					}
				}
			}
		}
		return "데이터 입력 완료";
	}

	private String allFileContent(List<MultipartFile> files) {
		int MAX_CHARACTERS = 4000; // 글자수제한
		StringBuilder fileContent = new StringBuilder("\n--- 첨부 파일 내용 ---\n"); // 첨부 파일 글자 이어 붙이기
		boolean contentFound = false; // 첨부파일에서 의미있는 내용 추출 됬는지 확인하는 변수

		// 파일 찾기임!!
		if (files != null) {
			for (MultipartFile file : files) {
				String originalFilename = file.getOriginalFilename();
				// 중복 추출 방지!
				if (originalFilename == null) {
					continue;
				}

				String lowerName = originalFilename.toLowerCase(); // 소문자 변환
				// 소문자로 변환해서 뒤에 붙은 파일명 찾기
				if (lowerName.endsWith(".txt") || lowerName.endsWith(".doc") || lowerName.endsWith(".docx")
						|| lowerName.endsWith(".hwp") || lowerName.endsWith(".pdf")) {

					try { // 티카 라이브러리가 파일안에 있는 내용을 문자열로 공백 없이 받아옴
						String content = tika.parseToString(file.getInputStream()).trim();

						// 파일 내용이 공백만 있는지 확인
						if (!content.isBlank()) {

							String limitedContent; // 글자수 제한
							boolean isTruncated = false; // 글자수가 4000자라 그전에 잘렷는지 알려주는 변수, 근데 굳이 안쓸래
							// 4000자 보다 길면 4000자까지 잘라라 그리고 트루 값을 남겨 알려줌
							if (content.length() > MAX_CHARACTERS) {
								limitedContent = content.substring(0, MAX_CHARACTERS) + "\n... [내용이 길어 중간 생략]...";
								isTruncated = true;
							} else {
								limitedContent = content; // 오류 날 수도있으니 넣어준 것
							}
							fileContent.append("[파일명 :").append(originalFilename).append("\n").append(limitedContent)
									.append("\n\n");
							log.info("정상 적으로 추출 성공");
						}
					} catch (Exception e) {
						log.error("문서 내용 읽기 실패");
					}
				} else if (lowerName.endsWith(".jpg") || lowerName.endsWith(".png")) {
					System.out.println("이미지 파일은 안됌");
				}
			}
		} // 있으면 문자열 반환, 없으면 공백 출력
		return contentFound ? fileContent.toString() : "";

	}

	private String getGuideContent(List<MultipartFile> files) {

		final String defaultGuide = "1. 오늘 목표:\\n2. 주요 성과:\\n3. 남은 일:";
		// 안에 파일 없으면 기본값으로 설정
		if (files == null || files.isEmpty())
			return defaultGuide;

		// 티카에서 파악하는 파일 형식임
		final List<String> tikaFile = List.of(".txt", ".doc", ".docx", ".hwp", ".pdf");

		for (MultipartFile file : files) {
			String originalFilename = file.getOriginalFilename();
			// 순회하면서 파일 이름을 변수에 넣는다. 없으면 위로 올림!
			if (originalFilename == null)
				continue;

			String lowerName = originalFilename.toLowerCase();
			// 여기 리스트 안에 있는 파일 중 하나라도 겹치는가를 확인!~
			boolean tilkaFiles = tikaFile.stream().anyMatch(lowerName::endsWith);
			// 만약에 겹쳐서 참이 되면 티카를 사용해서 문서 내용을 추출한다.
			if (tilkaFiles) {
				try {
					String content = tika.parseToString(file.getInputStream()).trim();
					log.info("첨부파일 추출 성공함 tika로");
					return content;
					// 실패하면 기본 양식 출력
				} catch (Exception e) {
					log.error("실패함 ㅠ");
					return defaultGuide;
				}
			}
		}
		return defaultGuide;
	}

//	private String getGuidelineAttachment(List<MultipartFile> files) {
//		// 만약에 파일 양식이 없을 경우 기본 양식을 활용
//		if(files == null || files.isEmpty()) {
//			return "1. 오늘 목표:\n2. 주요 성과:\n3. 남은 일:";
//		}
//		// txt로 찾는 파일 찾기 
//		for(MultipartFile file : files) { // subString(getOriginalFilename().lastIndexof(".")); 요렇게 확장자 상관 없이 이렇게 할 수 있음
//			if(file.getOriginalFilename() != null && file.getOriginalFilename().toLowerCase().endsWith(".txt")) {
//				try {
//					// 파일의 모든 데이터를 가져와서 우리말로 읽기 쉬운 문자열로 저장하삼 이라는 뜻 getBytes이거는 안에 내장된 함수임
//					String content = new String(file.getBytes(), StandardCharsets.UTF_8);
//					
//					System.out.println("첨부파일 양식 추출 성공 ");
//					
//					return content.trim();
//				} catch (Exception e) {
//					System.err.println("첨부파일 읽기 실패");
//					return "1. 오늘 목표:\n2. 주요 성과:\n3. 남은 일:";
//				}
//			}
//		}
//		return "1. 오늘 목표:\n2. 주요 성과:\n3. 남은 일:";
//	}

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
