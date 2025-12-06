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
        String rawAiResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();

        // ⭐️ [로그 추가] AI 응답 원본 로그 (디버깅용)
        System.out.println("--- AI 응답 원본 (RAW) ---");
        System.out.println(rawAiResponse);
        System.out.println("--------------------------");

        // 2. ⭐️ [강력 보강 로직] JSON 추출 로직

        // A. 먼저 마크다운 백틱(`)만 제거합니다.
        String cleanJson = rawAiResponse
            .trim()
            .replaceAll("```json|```", "")
            .trim();

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
}