package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.util.FileTextExtractor;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkChatAIService {

	// final로 생성자 주입
	private final ChatClient chatClient;
	private final FileTextExtractor fileTextExtractor;
	private final ObjectMapper objectMapper;

	// AI는 빌더로 주입
	public WorkChatAIService(ChatClient.Builder chatClientBuilder, FileTextExtractor fileTextExtractor) {
		this.chatClient = chatClientBuilder.build();
		this.fileTextExtractor = fileTextExtractor;
		this.objectMapper = new ObjectMapper();
	}

	// extractAndStructurize 추출 및 구조화라는 뜻 -> 파일 형식 추출
	public String extractAndStructurize(MultipartFile file) {
		String cleanJson = null;
		try {
			String rawText = fileTextExtractor.extractText(file);

			if (rawText.trim().isEmpty()) {
				return "문서에서 유효한 텍스트를 추출할 수 없습니다.";
			} // 프롬프트로 규칙을 정의.
			cleanJson = chatClient.prompt().system("""
					당신은 문서 구조화 전문가입니다.
					입력된 텍스트를 분석하여 문서의 양식과 필드를 스스로 파악하고, 모든 데이터를 JSON 형태로 추출하세요.
					
					규칙: 
					1. 미리 정의된 스키마 없이 텍스트 내용 기반으로 JSON 키를 만드세요.
					2. **표(Table) 형태만 JSON 배열([{}])로 표현**하세요.
					3. **특히 PDF 문서에서 추출된 것처럼 텍스트가 깨져있더라도, 문서 이미지(사용자 참조)를 참고하여 논리적인 표 구조(행/열)를 재구성**하고 배열로 만드세요.
					4. **단일 필드(예: '팀명', '작성자', '비고' 등)는 절대 배열로 만들지 말고, 단일 Key-Value 쌍으로 유지**하세요.
					5. **응답은 오직 순수한 JSON 객체({"..." : "..."})만 반환**해야 하며, 앞뒤에 어떠한 설명이나 Markdown 백틱(`)도 포함하지 마세요.
					6. 표 추출 시 비어 있는 셀은 null 또는 빈 문자열("")로 채우세요.
					
					""").user("문서 내용:\n---\n" + rawText).call().content();

		} catch (Exception e) {
			e.printStackTrace();
			return "문서 처리 중 오류 발생: " + e.getMessage();
		}
		return cleanJson.replace("Here is the JSON data in Markdown format:", "");
	}

	// 최종 생성보고서라는 뜻
	public String generateFinalReport(MultipartFile templateFile, String newContent) throws Exception {
		String templateStructureJson = extractAndStructurize(templateFile); // 텍스트 추출해서 양식 구조를 얻는다.

		if (templateStructureJson.startsWith("문서 처리 중 오류 발생")
				|| templateStructureJson.startsWith("문서에서 유효한 텍스트를 추출할 수 없습니다.")) {
			throw new RuntimeException("템플릿 양식 분석 오류: " + templateStructureJson);
		} // 추출된 값을 받아와서 템플릿을 만들고 내용을 채운다.
		return chatClient.prompt()
	            .system("""
	                당신은 문서 자동 생성 및 가공 전문가입니다.
	                당신의 역할은 두 개의 입력(필드 구조, 새 내용)을 받아 하나의 최종 JSON 보고서 객체를 생성하는 것입니다.
	                
	                **요구사항:**
	                1. 첫 번째 입력의 JSON 필드 구조(키)를 **그대로 유지**하세요.
	                2. 두 번째 입력의 **새 작업 내용을 요약 및 가공하여 적절한 필드에 값으로 채우세요.**
	                3. 원본 템플릿의 양식이 표(Table) 형태였고 그것이 JSON 배열([{}])로 추출되었다면, 해당 배열 구조를 유지하고 요약된 내용을 객체에 채우세요. 비어있는 배열 요소는 내용이 있다면 채우고, 내용이 없다면 비워두거나 제거하세요. (단, 템플릿 구조 자체는 변경하지 마세요.)
	                4. **응답은 오직 하나의 순수한 JSON 객체만 반환**해야 하며, 앞뒤에 어떠한 설명, Markdown 백틱(`), 또는 추가적인 JSON 객체도 포함하지 마세요.
	                5. 한국어로 내용을 채우세요.
	            """)
	            .user(String.format("### 템플릿 필드 구조 (JSON)\n%s\n\n### 새 작업 내용\n%s", templateStructureJson, newContent))
	            .call().content();
	}
}
