package com.example.demo.service;

import java.time.LocalDate;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.util.FileTextExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Service
public class WorkChatAIService {

	// final로 생성자 주입
	private final ChatClient chatClient;
	private final ObjectMapper objectMapper;
	private final TemplateMetaService templateMetaService;

	// AI는 빌더로 주입
	public WorkChatAIService(ChatClient.Builder chatClientBuilder, TemplateMetaService templateMetaService) {
		this.chatClient = chatClientBuilder.build();
		this.templateMetaService = templateMetaService;
		this.objectMapper = new ObjectMapper();
	}

	// 최종 생성보고서라는 뜻
	public String generateFinalReport(String templateId, String newContent) throws Exception {
		System.out.println("[AI] generateFinalReport templateId = " + templateId);
		String systemPrompt = templateMetaService.buildSystemPrompt(templateId);

		// 2) 유저 프롬프트: 사용자가 쓴 업무일지 원문 전달
		String userPrompt = """
				다음은 사용자가 작성한 업무일지 원문입니다.
				이 내용을 바탕으로 위 템플릿 JSON의 각 필드 값을 채워 주세요.

				---
				%s
				---
				""".formatted(newContent);

		// 3) AI 호출
		String rawAiResponse = chatClient.prompt().system(systemPrompt).user(userPrompt).call().content();

		// ⭐️ [로그 추가] AI 응답 원본 로그 (디버깅용)
		System.out.println("--- AI 응답 원본 (RAW) ---");
		System.out.println(rawAiResponse);
		System.out.println("--------------------------");

		// 2. ⭐️ [강력 보강 로직] JSON 추출 로직

		// A. 먼저 마크다운 백틱(`)만 제거합니다.
		String cleanJson = rawAiResponse.trim().replaceAll("```json|```", "").trim();

		// B. JSON 객체의 시작 부분인 '{' 또는 '['를 찾아 그 이전의 모든 텍스트를 버립니다.
		int jsonStartIndex = -1;
		int bracketIndex = cleanJson.indexOf('{');
		int arrayIndex = cleanJson.indexOf('[');

		if (bracketIndex != -1 && (arrayIndex == -1 || bracketIndex < arrayIndex)) {
			jsonStartIndex = bracketIndex; // '{'가 먼저 나옴
		} else if (arrayIndex != -1) {
			jsonStartIndex = arrayIndex; // '['가 먼저 나옴
		}

		if (jsonStartIndex != -1) {
			cleanJson = cleanJson.substring(jsonStartIndex).trim(); // 괄호부터 끝까지 추출
		} else {
			// '{' 나 '['이 전혀 없다면, 원본 반환 (JSON 아님)
			return rawAiResponse;
		}

		// C. ⭐️ [추가된 로직] JSON의 끝을 찾아 그 이후의 텍스트를 모두 버립니다.
		int endIndex = -1;
		int lastBrace = cleanJson.lastIndexOf('}');
		int lastBracket = cleanJson.lastIndexOf(']');

		// 가장 뒤에 나오는 닫는 괄호나 대괄호를 JSON의 끝으로 간주
		if (lastBrace > lastBracket) {
			endIndex = lastBrace;
		} else if (lastBracket > -1) {
			endIndex = lastBracket;
		}

		if (endIndex != -1) {
			// endIndex + 1을 하여 닫는 괄호도 포함하여 자르고, 그 이후의 모든 문자를 버립니다.
			cleanJson = cleanJson.substring(0, endIndex + 1).trim();
		} else {
			// 시작점은 찾았는데 끝점을 못 찾았다면, 불완전한 JSON이므로 원본 반환
			return rawAiResponse;
		}
		// ⭐️ [추가된 로직 끝]

		// 3. 추출된 cleanJson이 유효한지 최종 검증
		try {
			JsonNode root = objectMapper.readTree(cleanJson);

			// ⭐️ 오늘 날짜로 TPL1_DATE 강제 세팅
			if (root.isObject()) {
				ObjectNode obj = (ObjectNode) root;
				obj.put("TPL1_DATE", LocalDate.now().toString()); // 예: 2025-12-06 오늘 날짜 강제 삽입 하는거임
				cleanJson = objectMapper.writeValueAsString(obj);
			}

			System.out.println("--- AI 최종 반환 값 (CLEAN JSON + TODAY) ---");
			System.out.println(cleanJson);
			System.out.println("-------------------------------------------");
			return cleanJson;
		} catch (Exception jsonError) {
			System.err.println("최종 AI 응답이 유효한 JSON 형식이 아닙니다. 원본 반환.");

			// ⭐️ [로그 추가] 파싱 실패 시 로그
			System.out.println("--- AI 최종 반환 값 (RAW 원본 반환) ---");
			System.out.println(rawAiResponse);
			System.out.println("------------------------------------");
			return rawAiResponse;
		}
	}

	public String generateHandoverSummary(String worklogListText) {
		// 1) AI한테 역할 알려주는 시스템 프롬프트
		String systemPrompt = """
				당신은 업무 인수인계서를 작성하는 한국어 보조자입니다.
	            사용자가 넘겨주는 텍스트는 일정 기간 동안 작성한 업무일지 목록입니다.
	
	            이 내용을 바탕으로 인수인계서의 "인수인계 사항"에 들어갈 내용을 작성하세요.
	
	            출력 형식(예시 구조):
	            1. 현재 담당 중인 주요 업무
	               - ...
	               - ...
	
	            2. 후임자가 이어서 해야 할 작업
	               - ...
	               - ...
	
	            3. 주의해야 할 이슈 / 위험 요소
	               - ...
	               - ...
	
	            4. 참고해야 할 시스템 / 문서 / 계정 정보
	               - ...
	               - ...
	
	            작성 규칙:
	            1. 각 번호(1,2,3,4)는 반드시 줄의 맨 앞에서 시작합니다.
	            2. 각 번호 아래 내용은 여러 개의 '- ' 불릿으로 작성합니다.
	            3. 각 번호 블록 사이에는 반드시 빈 줄(\\n\\n)을 한 줄 넣습니다.
	            4. 한 문단이 너무 길어지지 않게 2~3문장 정도로 나누세요.
	            5. "###" 같은 마크다운 제목은 사용하지 마세요.
	            6. 전체 분량은 A4 1페이지 안에 들어갈 정도로 적당히 요약합니다.
	            """;

		// 2) 실제 업무일지 목록을 포함하는 유저 프롬프트
		String userPrompt = """
				아래는 사용자가 선택한 기간 동안 작성한 업무일지 목록입니다.

				이 목록을 보고, 위 규칙에 맞게
				"인수인계 사항"에 들어갈 내용을 한국어로 정리해 주세요.

				----- 업무일지 목록 시작 -----
				%s
				----- 업무일지 목록 끝 -----
				""".formatted(worklogListText);

		String result = chatClient
	            .prompt()
	            .system(systemPrompt)
	            .user(userPrompt)
	            .call()
	            .content();

	    // 🔻🔻🔻 여기부터 "후처리" 추가한 부분 🔻🔻🔻
	    if (result != null) {
	        // 혹시 제목 같은 거 붙어오면 제거
	        result = result.replace("### 인수인계 사항", "");
	        // 필요하면 모든 ###제목 날려버리기
	        result = result.replaceAll("###.*\\n", "");

	        // 줄바꿈 정리 (윈도우/리눅스 섞여도 안전하게)
	        result = result.replace("\r\n", "\n");

	        // 너무 많은 개행 줄이기
	        result = result.replace("\n\n\n", "\n\n");

	        // 한 줄 개행을 두 줄 개행으로 => 문단 사이가 넉넉하게 보이게
	        result = result.replace("\n", "\n\n");
	    }

	    if (result == null || result.isBlank()) {
	        return worklogListText;   // 그래도 실패하면 재료 텍스트라도 반환
	    }
	    return result.trim();
	}
}