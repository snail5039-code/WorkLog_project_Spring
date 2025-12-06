package com.example.demo.controller;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.service.DocxTemplateService;
import com.example.demo.service.TemplateValueService;

@RestController
@RequestMapping("/api")
public class TestDocxController {

    private final DocxTemplateService docxTemplateService;
    private final TemplateValueService templateValueService;
    
    public TestDocxController(DocxTemplateService docxTemplateService, TemplateValueService templateValueService) {
        this.docxTemplateService = docxTemplateService;
        this.templateValueService = templateValueService;
    }

    @GetMapping("/test-docx")
    public ResponseEntity<byte[]> testDocx() throws IOException {
    	System.out.println(">>> /api/test-docx 호출됨");
        // 🔹 워드 안에 있는 플레이스홀더랑 "완전 똑같이" 써야 함!
        Map<String, Object> raw = new HashMap<>();
        raw.put("TPL1_DATE", "2025-12-06");
        raw.put("TPL1_MON_TASK_TODAY", "월요일 오늘 한 일 테스트(auto)");
        raw.put("TPL1_MON_TASK_NEXT", "월요일 내일 할 일 테스트(auto)");
        raw.put("TPL1_TUE_TASK_TODAY", "화요일 오늘 한 일 테스트(auto)");
        raw.put("TPL1_TUE_TASK_NEXT", "화요일 내일 할 일 테스트(auto)");
        raw.put("TPL1_WED_TASK_TODAY", "수요일 오늘 한 일 테스트(auto)");
        raw.put("TPL1_WED_TASK_NEXT", "수요일 내일 할 일 테스트(auto)");
        raw.put("TPL1_THU_TASK_TODAY", "목요일 오늘 한 일 테스트(auto)");
        raw.put("TPL1_THU_TASK_NEXT", "목요일 내일 할 일 테스트(auto)");
        raw.put("TPL1_FRI_TASK_TODAY", "금요일 오늘 한 일 테스트(auto)");
        raw.put("TPL1_FRI_TASK_NEXT", "금요일 내일 할 일 테스트(auto)");
        raw.put("TPL1_SAT_TASK_TODAY", "토요일 오늘 한 일 테스트(auto)");
        raw.put("TPL1_SAT_TASK_NEXT", "토요일 내일 할 일 테스트(auto)");
        raw.put("TPL1_SUGGESTIONS", "특이사항/건의사항 테스트입니다.(auto)");
        
        Map<String, String> values = templateValueService.buildValuesFromRaw(raw);
        // 🔹 resources/templates/ 안에 있는 파일 이름 그대로 써주기
        byte[] fileBytes =
                docxTemplateService.fileTemplate("업무일지양식1.docx", values);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                )
        );
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("업무일지_테스트.docx", StandardCharsets.UTF_8)
                        .build()
        );

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}
