package com.example.demo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.dto.TemplateUsageDto;
import com.example.demo.dto.WorkLog;
import com.example.demo.service.DocxTemplateService;
import com.example.demo.service.FileAttachService;
import com.example.demo.service.TemplateValueService;
import com.example.demo.service.WorkChatAIService;
import com.example.demo.service.WorkLogService;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

@Slf4j // 로킹 어노테이션
@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@RequestMapping("/api")
public class WorkLogController {

	@Value("${file.upload-dir}") 
	private String uploadDir;
	  
	private final WorkChatAIService workChatAIService;
	private FileAttachService fileAttachService;
	private WorkLogService workLogService;
	private final TemplateValueService templateValueService;
	private final DocxTemplateService docxTemplateService;
	
	// 의존성 주입
	public WorkLogController(WorkLogService workLogService, FileAttachService fileAttachService,
			WorkChatAIService workChatAIService, TemplateValueService templateValueService, DocxTemplateService docxTemplateService) {
		this.workLogService = workLogService;
		this.fileAttachService = fileAttachService;
		this.workChatAIService = workChatAIService;
		this.templateValueService = templateValueService;
		this.docxTemplateService = docxTemplateService;
	}
	
	// 💡 실제로 쓸 엔드포인트 
    @GetMapping("/worklogs/{id}/download/{templateId}")
    public ResponseEntity<byte[]> downloadTemplate(@PathVariable int id, @PathVariable String templateId) throws IOException {
        System.out.println(">>> /api/worklogs/" + id + "/download/template1 호출됨");
        // 디비에서 해당 업무일지 가져오는 것
        WorkLog log = workLogService.showDetail(id);
        if(log == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "업무일지를 찾을 수 없습니다.");
        }
        String summaryJson = log.getSummaryContent();
        if(summaryJson == null || summaryJson.isEmpty()) {
        	throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "이 업무일지에는 템플릿 데이터를 위한 요약이 없습니다.");
        }
        // 나중에 양식 더 추가 시키기
        String docxFileName;
        switch (templateId.toUpperCase()) {
        case "TPL1": docxFileName = "업무일지양식1.docx"; break;
        case "TPL3": docxFileName = "업무일지양식3.docx"; break;
        case "TPL4": docxFileName = "업무일지양식4.docx"; break;
        case "TPL5": docxFileName = "업무일지양식5.docx"; break;
        case "TPL6": docxFileName = "업무일지양식6.docx"; break;
        case "TPL7": docxFileName = "업무일지양식7.docx"; break;
		default:
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "지원하지 않는 템플릿입니다." + templateId);
		}
        
        // 자동 치환 메서드 호출
        Map<String, String> values = templateValueService.buildValuesFromJson(summaryJson);
        // 3) 템플릿 적용
        byte[] fileBytes =
                docxTemplateService.fileTemplate(docxFileName, values);

        // 4) 응답 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
        );
        headers.setContentDisposition(
                ContentDisposition.attachment()
                		.filename("worklog_" + id + "_" + templateId.toUpperCase() + ".docx", StandardCharsets.UTF_8)
                		.build()
        );

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }

	

	@PostMapping("/usr/work/workLog") // MultipartFile 이거는 스프링부트 내장이라서 바로 사용 가능함, 리액트에서 multiple를 받아온거!
	public String writeWorkLog(String title, String mainContent, String sideContent, String templateId, List<MultipartFile> files,
			HttpSession session) {
		// 여기는 ai한테 입력된 값 넘기는 곳!
		String finalAiReport = null;
		// ai 처리를 위해 템플릿 파일, 내용을 준비 
		MultipartFile templateFile = null; //combinedNewContent 결합된 새로운 내용
		String combinedNewContent = "제목: " + title + "\n\n" + mainContent + "\n\n보조 내용: " + sideContent;
			
		
			String effectiveTemplateId;
			try {
				
				effectiveTemplateId = (templateId == null || templateId.isBlank())
						? "TPL1"
						: templateId;
				finalAiReport = this.workChatAIService.generateFinalReport(effectiveTemplateId, combinedNewContent);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("AI 보고서 생성 중 오류 발생, 원본 내용 저장:" + e.getMessage());
		        // DB에서 summaryContent NOT NULL 이라면 최소한 빈 JSON이라도 넣어주자
		        finalAiReport = "{}";
		        effectiveTemplateId = "TPL1";
			}
		
		int memberIdObj = (int) session.getAttribute("logindeMemberId");
		
		// MultipartFile 이거는 따로 테이블 만들어서 보관해야됌!
		WorkLog workLogData = new WorkLog();
		workLogData.setTitle(title);
		workLogData.setMainContent(mainContent);
		workLogData.setSideContent(sideContent);
		
		workLogData.setTemplateId(effectiveTemplateId);
		
		// ai가 생성한 최종 보고서 담기 
		if(finalAiReport != null && !finalAiReport.trim().isEmpty()) {
			workLogData.setSummaryContent(finalAiReport);
		} else {
			workLogData.setSummaryContent("{}");
		}
		
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
	
	// 파일 다운로드 하게하기
	@GetMapping("/usr/work/download/{storedFilename}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String storedFilename) {
		// db 저장된 파일명을 이용 원본 파일명 조회 하는 것!
		String originalFilename = fileAttachService.getOriginalFilename(storedFilename);
		
		if(originalFilename == null) {
			System.out.println("파일을 찾을 수 없음!");
			log.error("파일을 찾을 수가 없음..");
		}
		// 파일 경로 찾는 것임!
		Path filePath = Paths.get(uploadDir).resolve(storedFilename).normalize();
		log.info("시도된 파일 다운로드 경로: {}", filePath.toAbsolutePath()); // toAbsolutePath()를 사용해 절대 경로를 확인	
		Resource resource;
		
		try {
			resource = new UrlResource(filePath.toUri());
		} catch (Exception e) {
			log.error("파일 경로가 올바르지 않음: {}", storedFilename, e);
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일 경로가 올바르지 않습니다.");
		}
		// exists 실제로 있는지 파일이, isReadable 권한이 있는지 
		if(!resource.exists() || !resource.isReadable()) {
			log.error("파일을 찾을 수가 없음..");
			System.out.println("파일을 찾을 수 없음!");
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다.");
		}
		
		// 다운로드 해야할 파일, 파일 이름 알려주는 역할임!
		String contentDisposition = "";
		
		// 브라우저한테 인코딩해서 파일 보낼거임!
		try {
			// ISO-8859-1 이걸로 변환해서 안보내면 깨짐 
			 String encodedFilename = new String(originalFilename.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);	
			 // attachment;filename=\ 요거는 텍스트 명령어라서 규칙임, 첨부파일이니깐 다운로드해라, 이름은 저거임 이라는 것!
			 contentDisposition =  "attachment;filename=\"" + encodedFilename + "\"";
		} catch (Exception e) {
			log.warn("응 인코딩 실패!");
			contentDisposition =  "attachment;filename=\"" + originalFilename + "\"";
		}
		// contentType(MediaType.APPLICATION_OCTET_STREAM) 이거는 바이너리 파일임. 약속된거라서 그냥 쓰면 됌
		// HttpHeaders.CONTENT_DISPOSITION, contentDisposition 이것도 약속임 파일 이름 알려주는 거 위에 다운로드 하라는 것도 같이 그래서 실제 데이터를 body(resource) 요기에 담는거!
		return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition).body(resource);
		
	}
	
	@GetMapping("/usr/workLog/myPageSummary")
	public Map<String, Object> getMyPageSummary(HttpSession session, @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
		int memberId = -1;
		
		memberId = (int) session.getAttribute("logindeMemberId");
		
		if(memberId == -1) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
		}
		
		
		
		List<WorkLog> myWorkLogs = workLogService.getMyWorkLogsPaged(memberId, page, size);
		
		int totalCount = workLogService.getMyWorkLogsCount(memberId); // 내가 쓴 총 게시글 갯수
		int thisMonthCount = workLogService.getThisMonthCount(memberId); // 이번달 게시글 갯수
		
		LocalDateTime lastWritten = workLogService.getLastWrittenDate(memberId);
		
		List<TemplateUsageDto> topTemplates = workLogService.getTopTemplates(memberId);
		
		Map<String, Object> summary = new HashMap<>();
		summary.put("totalCount", totalCount);
		summary.put("thisMonthCount", thisMonthCount);
		summary.put("lastWrittenDate", lastWritten);
		summary.put("topTemplates", topTemplates);
		
		Map<String, Object> result = new HashMap<>();
		result.put("summary", summary);
		result.put("myWorkLogs", myWorkLogs);
		
		return result;
	}
	
	
	@GetMapping("/usr/work/list")
	public Map<String, Object> showList(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "1") int size, @RequestParam(required = false) Integer boardId) {
		if(page < 1) page = 1;
		if(size <= 0 || size > 100) size = 10;
		
		List<WorkLog> items = workLogService.getBoardListPaged(boardId, page, size);
		int totalCount = workLogService.getBoardListCount(boardId);
		
		Map<String, Object> result = new HashMap<>();
	    result.put("items", items);
	    result.put("totalCount", totalCount);

	    return result;
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