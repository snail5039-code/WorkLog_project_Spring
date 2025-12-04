package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.controller.WorkLogController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class DocumentGeneratorService {
	
	private final WorkChatAIService workChatAIService;
	
	public DocumentGeneratorService(WorkChatAIService workChatAIService) {
		this.workChatAIService = workChatAIService;
	}
	// 문서 생성이라는 의미
	public byte[] generateDocxReport(MultipartFile templateFile, Map<String, Object> docxData) throws Exception {
	    // 템플릿 파일 유효성 검사
        if (templateFile == null || templateFile.isEmpty()) {
            throw new IllegalArgumentException("템플릿 파일이 유효하지 않습니다.");
        }
		
		// List 타입을 제외하고 모든 값을 String으로 변환합니다.
        Map<String, String> stringData = docxData.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            // List 타입은 반복 테이블 처리용이므로 제외합니다.
            .filter(entry -> !(entry.getValue() instanceof List)) 
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toString() // 모든 값을 String으로 강제 변환
            )); // 고오그읍 문법임, 안에 널이면 제외, 그리고 리스트 형식은 따로 빼놧으니깐 제외 그래서 다시 맵 안에 있는 것들을 스트링으로 강제 반환한다는 것임
        // 2. [핵심 로직] 업로드된 파일의 스트림을 바로 읽고, 결과를 저장할 스트림을 준비합니다.
        //    try-with-resources 구문을 사용하여 스트림이 자동으로 닫히도록 처리합니다.
		try (InputStream templateStream = templateFile.getInputStream(); // 👈 업로드된 MultipartFile에서 파일 내용을 바로 읽어옵니다. (이전 오류 해결)
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            // 3. 템플릿 Docx 파일을 로드합니다.
            WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateStream);

            // 4. Word 문서의 메인 파트(본문)에 접근합니다.
            MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
            // 5. 템플릿 내의 ${변수명} 플레이스홀더를 stringData 맵의 값으로 치환합니다.
            documentPart.variableReplace(stringData); 
            // 6. 데이터가 채워진 Word 문서를 메모리 스트림(baos)에 저장합니다.
            wordMLPackage.save(baos); 
            // 7. 메모리 스트림의 내용을 byte 배열로 변환하여 반환합니다.
            return baos.toByteArray(); 

        } catch (Exception e) {
            // 오류 발생 시 로깅하고 예외를 다시 던져서 트랜잭션 롤백 등을 유도합니다.
            log.error("Docx 보고서 생성/처리 중 심각한 오류 발생", e);
            throw new Exception("Docx 보고서 생성에 실패했습니다: " + e.getMessage(), e);
        }
	}
}