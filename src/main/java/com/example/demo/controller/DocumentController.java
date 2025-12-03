package com.example.demo.controller;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.DocumentGeneratorService;

@RestController
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // 쿠키 설정
@RequestMapping("/api")
public class DocumentController {
	private final DocumentGeneratorService documentGeneratorService;

	public DocumentController(DocumentGeneratorService documentGeneratorService) {
		this.documentGeneratorService = documentGeneratorService;
	}

//	@PostMapping("/usr/work/workLog") 이거 때문에 잠깐 오류남
	public ResponseEntity<String> generateWorkLog(String title, String mainContent, String sideContent, String author,
			String position, String reportId, String documentType, List<MultipartFile> files) {
		
		// 원하는 양식 골르기
		final String TEMPLATE_FILE_NAME;
		
		switch (documentType) {
		case "1": 
			TEMPLATE_FILE_NAME = "업무일지양식3.docx";
			break;
		case "2": 
			TEMPLATE_FILE_NAME = "업무일지양식4.docx";
			break;
		case "3": 
			TEMPLATE_FILE_NAME = "업무일지양식5.docx";
			break;
		case "4": 
			TEMPLATE_FILE_NAME = "업무일지양식6.docx";
			break;
		case "5": 
			TEMPLATE_FILE_NAME = "업무일지양식7.docx";
			break;
		case "6" :
		default:
            TEMPLATE_FILE_NAME = "업무일지양식1.docx";
            break; // 기본 값 세팅한거임
		}
		try {
			// 1. 문서 템플릿에 채워 넣을 실제 데이터(templateData)를 준비.
            Map<String, Object> templateData = new HashMap<>();
	        
            // 1-1. 정형 정보 직접 매핑 (사용자 입력/시스템 값) <- 사용자가 입력하거나 로그인 정보로 가져오기
            templateData.put("author", author); 
            templateData.put("date", LocalDate.now().toString());
            templateData.put("position", position); 
            templateData.put("report_id", reportId);
            templateData.put("dept", "문서생성팀"); // 임시 고정 값
            
            // 1-2. 비정형 정보 (mainContent) 매핑, 이건 실제 작성된 자리
            //      (실제로는 AI 분석을 통해 구조화된 데이터로 변환되어 여기에 들어갑니다.)
            templateData.put("TPL1_MON_TASK_TODAY", mainContent); 
            
            // 밑에는 ai가 분석해서 들어갈 자리라서 그럼
            // 1-3. 나머지 필드들은 임시로 빈 문자열로 초기화 (AI 분석 결과가 없다고 가정)
            templateData.put("TPL1_MON_TASK_NEXT", ""); 
            templateData.put("TPL1_SUGGESTIONS", "");
            templateData.put("TPL1_TUE_TASK_TODAY", "");
            templateData.put("TPL1_TUE_TASK_NEXT", "");
            templateData.put("TPL1_WED_TASK_TODAY", "");
            templateData.put("TPL1_WED_TASK_NEXT", "");
            templateData.put("TPL1_THU_TASK_TODAY", "");
            templateData.put("TPL1_THU_TASK_NEXT", "");
            templateData.put("TPL1_FRI_TASK_TODAY", "");
            templateData.put("TPL1_FRI_TASK_NEXT", "");
            templateData.put("TPL1_SAT_TASK_TODAY", "");
            templateData.put("TPL1_SAT_TASK_NEXT", "");
            
            // 2. 파일 저장 경로 및 이름 설정
            String outputFileName = TEMPLATE_FILE_NAME.replace(".docx", "_") + reportId + "_completed.docx";
            // 실제로는 따로 저장소를 만들어서 해야됌!
            String outputFilePath = "C:/temp/output/" + outputFileName;
            
            // 3. DocumentGeneratorService 호출하여 문서 생성
            String resultPath = documentGeneratorService.generatorDocument(
                TEMPLATE_FILE_NAME, 
                templateData, // 👈 준비된 실제 데이터를 전달합니다.
                outputFilePath
            );
            
         // 4. 첨부 파일 처리 로직
            if (files != null && !files.isEmpty()) {
                // 4-1. 첨부 파일 저장 폴더 생성 (고유 ID로 폴더를 만들어 관리)
                // 🚨 경로를 직접 문자열로 사용
                String attachmentDir = "C:/temp/output/attachments/" + reportId + "/";
                Files.createDirectories(Paths.get(attachmentDir)); // 폴더 생성

                int savedCount = 0;
                for (MultipartFile file : files) {
                    if (!file.isEmpty()) {
                        // 4-2. 파일명 중복 방지를 위해 UUID를 원본 파일명 앞에 추가
                        String uuid = UUID.randomUUID().toString();
                        String originalFileName = file.getOriginalFilename();
                        String attachmentFileName = uuid + "_" + originalFileName; // UUID_원본파일명.확장자
                        
                        // 4-3. 파일 저장 경로 설정
                        String savePath = attachmentDir + attachmentFileName; // 쉽게 말해서 전체 주소임 "C:/temp/.../RPT-1234/UUID_보고서.pdf 이런느낌
                        
                        // 4-4. 파일 저장
                        Files.write(Paths.get(savePath), file.getBytes()); // 파일을 저기 주소에 저장하셈
                        savedCount++;// 그냥 잘 저장됬는지 확인하려고 만듬ㅋ
                    }
                }
                System.out.println("첨부 파일 " + files.size() + "개 중 " + savedCount + "개가 " + attachmentDir + "에 저장되었습니다.");
            }

            return ResponseEntity.ok("업무일지 생성이 완료되었습니다. (사용된 양식: " + documentType + "번)");

        } catch (Exception e) {
            // 오류 발생 시 상세 메시지와 함께 500 응답 반환
            e.printStackTrace();
            return ResponseEntity.status(500).body("문서 생성 중 오류 발생: " + e.getMessage());
        }
	}
}
