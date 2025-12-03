package com.example.demo.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.springframework.stereotype.Service;

@Service
public class DocumentGeneratorService {
	
	private final WorkChatAIService workChatAIService;
	
	public DocumentGeneratorService(WorkChatAIService workChatAIService) {
		this.workChatAIService = workChatAIService;
	}
	// 문서 생성이라는 의미
	public String generatorDocument(String templateFileName, Map<String, Object> aiData, String outputFilePath) throws Exception {
		// 가공된 데이터 가져오기!
		Map<String, Object> finalData = workChatAIService.mapDataForTemplate(templateFileName, aiData);
		
		// List 타입을 제외하고 모든 값을 String으로 변환합니다.
        Map<String, String> stringData = finalData.entrySet().stream()
            .filter(entry -> entry.getValue() != null)
            // List 타입은 반복 테이블 처리용이므로 제외합니다.
            .filter(entry -> !(entry.getValue() instanceof List)) 
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().toString() // 모든 값을 String으로 강제 변환
            )); // 고오그읍 문법임, 안에 널이면 제외, 그리고 리스트 형식은 따로 빼놧으니깐 제외 그래서 다시 맵 안에 있는 것들을 스트링으로 강제 반환한다는 것임
		
		InputStream templateStream = getTemplateStream(templateFileName);
		
		// Word 문서 로드, 익셉션 안던지면 오류남
	    WordprocessingMLPackage wordMLPackage = WordprocessingMLPackage.load(templateStream);
	    
	    // 본문 내용 접근
	    MainDocumentPart documentPart = wordMLPackage.getMainDocumentPart();
		
	    documentPart.variableReplace(stringData); //최종 데이터 삽입
	    
	    File outputFile = new File(outputFilePath); // 저장 대상 객체 생성
	    wordMLPackage.save(outputFile); // 저장한거임 여기다가
	    // docx4j 라이브러리 사용 시 이런식으로 저장됌, 그래서 겁나 헷갈림
		return outputFilePath; // 요 리턴값은 최종 저장한 파일 경로를 나타낸 거임
	}
	
	
	// 통로 열어서 템플릿 파일 가져오기!
	private InputStream getTemplateStream(String templateFileName) {
		try {
			// 저 파일 찾게 통로 열어!
			InputStream is = getClass().getClassLoader().getResourceAsStream(templateFileName);
		
			if(is == null) {
				throw new FileNotFoundException("템플릿을 찾을 수 없습니다.");
			}
			return is;
		} catch (FileNotFoundException e) {
			throw new RuntimeException("템플릿 로드 실패!");
		}
	}
}
