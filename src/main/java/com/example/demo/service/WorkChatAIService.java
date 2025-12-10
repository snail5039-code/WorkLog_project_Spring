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

		// 응답이 비거나 null이면 바로 fallback JSON
	    if (rawAiResponse == null || rawAiResponse.isBlank()) {
	        return buildFallbackJson(templateId, "");
	    }

	    // A. 마크다운 백틱 제거
	    String cleanJson = rawAiResponse.trim()
	            .replaceAll("```json", "")
	            .replaceAll("```", "")
	            .trim();

	    // B. JSON 시작 위치 찾기 ({ 또는 [)
	    int jsonStartIndex = -1;
	    int braceIndex = cleanJson.indexOf('{');
	    int bracketIndex = cleanJson.indexOf('[');

	    if (braceIndex != -1 && (bracketIndex == -1 || braceIndex < bracketIndex)) {
	        jsonStartIndex = braceIndex;   // { 가 먼저
	    } else if (bracketIndex != -1) {
	        jsonStartIndex = bracketIndex; // [ 가 먼저
	    }

	    if (jsonStartIndex == -1) {
	        // JSON 시작 자체를 못 찾으면 → 공통 fallback JSON
	        System.out.println("[AI] JSON 시작 문자({ 또는 [)를 찾지 못했습니다. fallback 반환");
	        return buildFallbackJson(templateId, rawAiResponse);
	    }

	    cleanJson = cleanJson.substring(jsonStartIndex).trim();

	    // C. JSON 끝 위치 찾기 (마지막 } 또는 ])
	    int lastBrace = cleanJson.lastIndexOf('}');
	    int lastBracket = cleanJson.lastIndexOf(']');
	    int endIndex = Math.max(lastBrace, lastBracket);

	    if (endIndex == -1) {
	        // 닫는 괄호 못 찾으면 → 공통 fallback JSON
	        System.out.println("[AI] JSON 닫는 문자(} 또는 ])를 찾지 못했습니다. fallback 반환");
	        return buildFallbackJson(templateId, rawAiResponse);
	    }

	    cleanJson = cleanJson.substring(0, endIndex + 1).trim();

	    // 3. 최종 JSON 검증 + (필요하면 날짜 필드 주입)
	    try {
	        JsonNode root = objectMapper.readTree(cleanJson);

	        if (root.isObject()) {
	            ObjectNode obj = (ObjectNode) root;

	            // ✅ 기존에 쓰던 날짜 필드 있으면 여기서 세팅
	            //    (지금은 예시로 TPL1_DATE 유지, 필요 없으면 이 줄 지워도 됨)
	            obj.put("TPL1_DATE", LocalDate.now().toString());

	            cleanJson = objectMapper.writeValueAsString(obj);
	        }

	        System.out.println("--- AI 최종 반환 값 (CLEAN JSON) ---");
	        System.out.println(cleanJson);
	        System.out.println("-----------------------------------");

	        return cleanJson;
	    } catch (Exception jsonError) {
	        System.err.println("[AI] 최종 AI 응답이 유효한 JSON 형식이 아닙니다. fallback JSON 반환.");
	        jsonError.printStackTrace();
	        return buildFallbackJson(templateId, rawAiResponse);
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
	
	// ✨ 어떤 템플릿이든 공통으로 쓰는 fallback JSON
	private String buildFallbackJson(String templateId, String rawAiResponse) {
	    try {
	        ObjectNode root = objectMapper.createObjectNode();

	        // 메타 정보 (어떤 템플릿에서 실패했는지)
	        ObjectNode meta = objectMapper.createObjectNode();
	        meta.put("templateId", templateId);
	        meta.put("status", "ERROR");
	        root.set("_meta", meta);

	        // 사람이 읽을 수 있는 메시지
	        root.put("message", "AI 요약 JSON 생성 실패");

	        // AI가 실제로 뭐라고 했는지 전체 저장 (디버깅용, 화면 표시용)
	        root.put("raw", rawAiResponse);

	        return objectMapper.writeValueAsString(root);
	    } catch (Exception e) {
	        // 여기서까지 터질 일은 거의 없음. 그래도 최후의 수단으로 최소 JSON 반환
	        return """
	               {
	                 "_meta": { "status": "ERROR", "templateId": "%s" },
	                 "message": "AI 요약 JSON 생성 실패 (fallback 내부 에러)",
	                 "raw": ""
	               }
	               """.formatted(templateId);
	    }
	}

}