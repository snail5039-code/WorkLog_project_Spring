package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.WorkLogDao;
import com.example.demo.dto.Template;
import com.example.demo.util.FileTextExtractor;
import com.fasterxml.jackson.databind.ObjectMapper; 

@Service
public class WorkChatAIService {

    // final로 생성자 주입
    private final ChatClient chatClient;
    private final FileTextExtractor fileTextExtractor;
    private final ObjectMapper objectMapper;
    private final WorkLogDao workLogDao;

    // AI는 빌더로 주입
    public WorkChatAIService(ChatClient.Builder chatClientBuilder, FileTextExtractor fileTextExtractor, WorkLogDao workLogDao) {
        this.chatClient = chatClientBuilder.build();
        this.fileTextExtractor = fileTextExtractor;
        this.objectMapper = new ObjectMapper();
        this.workLogDao = workLogDao;
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
        // 이 부분도 다른 불필요한 텍스트가 붙을 경우를 대비해 추출 로직을 적용하는 것이 좋지만,
        // 현재 generateFinalReport의 로직이 더 중요하므로 임시로 유지
        return cleanJson.replace("Here is the JSON data in Markdown format:", "");
    }

    // 최종 생성보고서라는 뜻
    public String generateFinalReport(MultipartFile templateFile, String newContent) throws Exception {
        String templateStructureJson = extractAndStructurize(templateFile); // 텍스트 추출해서 양식 구조를 얻는다.

        if (templateStructureJson.startsWith("문서 처리 중 오류 발생")
                || templateStructureJson.startsWith("문서에서 유효한 텍스트를 추출할 수 없습니다.")) {
            throw new RuntimeException("템플릿 양식 분석 오류: " + templateStructureJson);
        } // 추출된 값을 받아와서 템플릿을 만들고 내용을 채운다.
        
        // 1. AI 응답을 받고 변수에 저장합니다.
        String rawAiResponse = chatClient.prompt()
            .system("""
                    당신은 문서 자동 생성 및 가공 전문가입니다. 당신의 목표는 **요청된 구조에 100% 일치하는 유효한 JSON 객체**만을 출력하는 것입니다.
                    
                    **[출력 형식 및 필수 요구사항]**
                    1. 첫 번째 입력의 JSON 필드 구조(키)를 **절대 변경하지 말고 그대로 유지**하세요.
                    2. 두 번째 입력의 **새 작업 내용을 요약 및 가공하여 적절한 필드에 값으로 채우세요.**
                    3. 원본 템플릿이 JSON 배열([{}])이었다면 배열 구조를 유지하고 내용을 채우세요.
                    4. 한국어로 내용을 채우세요.
                    
                    **[출력 금지 사항: 매우 중요]**
                    * **절대 JSON 객체 외부**에 다음과 같은 **어떠한 설명도 포함하지 마십시오.** (예: "Here is the JSON report:", "AI 생성 보고서입니다:", "```json", 등)
                    * **오직 유효하고 순수한 JSON 객체**로 응답을 시작하고 끝내야 합니다.
                    * 응답은 **반드시 `{` 문자로 시작**하고 **`}` 문자로 끝나야** 합니다.
                """)
            .user(String.format("### 템플릿 필드 구조 (JSON)\n%s\n\n### 새 작업 내용\n%s", templateStructureJson, newContent))
            .call().content();

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
        // 간단하게 누가 먼저 나오는지 찾아서 그 외 밖에 있는 것들 잘라낸다는 뜻// 나중에 다시 한번더 이해해보자
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
            objectMapper.readTree(cleanJson);
            
            // ⭐️ [로그 추가] JSON 추출 성공 시 로그
            System.out.println("--- AI 최종 반환 값 (CLEAN JSON) ---");
            System.out.println(cleanJson);
            System.out.println("------------------------------------");
            return cleanJson; // 유효한 JSON을 반환
        } catch (Exception jsonError) {
            System.err.println("최종 AI 응답이 유효한 JSON 형식이 아닙니다. 원본 반환.");
            
            // ⭐️ [로그 추가] 파싱 실패 시 로그
            System.out.println("--- AI 최종 반환 값 (RAW 원본 반환) ---");
            System.out.println(rawAiResponse);
            System.out.println("------------------------------------");
            return rawAiResponse; 
        }
    }
    
    // 이 밑에 메서드들은 최종적으로 입력해논 문서 안 내용에 정확한 위치에 들어가기 위해 하는 것임
    // ai가 저장된 첨부파일을 가져와 그리는 작업을 하려고 함!
    public Map<String, Object> mapDataForTemplate(String templateFileName, Map<String, Object> aiData) {
    	// 첨푸파일에 넣어준 값과 매칭 시켜 가져온다.
    	List<Template> mappings = this.workLogDao.selectMappingsByFileName(templateFileName);

    	// 최종적으로 전달할 데이터임
    	Map<String, Object> finalData = new HashMap<>();
    	
    	// 안에 있는 1:1 매칭 되있는 값을 데이터 변환 
    	for(Template mapping : mappings) {
    		String jsonKey = mapping.getJsonKey();
    		String tplKey = mapping.getPlaceholder();
    		
    		if(aiData.containsKey(jsonKey)) {
    			//ai 데이터에서 값을 꺼내 최종 맵에 넣는다는 것!
    			finalData.put(tplKey, aiData.get(jsonKey));
    		}
    	} // <- 여기 까지는 단순한 데이터들은 매칭을 시키는 것이고 밑에 쪽에는 목록형 데이터들이 있으면 어려우니 
    	//convertListToString 이거를 호출해서 보기좋게 정리한다음 스위치문에서 하는것임
    	
    	// 반복적인 것들, 한칸에 여러줄, 반복 되는 표 등을 위해 만든 것
    	switch (templateFileName) {
        case "업무일지양식4.docx":
            // 목록 (List) 데이터를 줄바꿈 문자열로 변환하여 단일 필드에 삽입
            convertListToString(aiData, finalData, "today_tasks_list", "TPL4_TODAY_TASKS");
            convertListToString(aiData, finalData, "next_plan_list", "TPL4_NEXT_PLAN");
            break;
            
        case "업무일지양식5.docx":
            // **반복 테이블 데이터** 처리: List<Map> 자체를 그대로 finalData에 복사하여 문서 엔진에 전달
            if (aiData.containsKey("tasks") && aiData.get("tasks") instanceof List) {
                finalData.put("tasks", aiData.get("tasks")); 
            }
            break;
            
        case "업무일지양식6.docx":
            // List를 줄바꿈 문자열로 변환하여 단일 필드에 삽입
            convertListToString(aiData, finalData, "today_issue", "TPL6_TODAY_ISSUE");
            convertListToString(aiData, finalData, "today_tasks", "TPL6_TODAY_TASKS");
            convertListToString(aiData, finalData, "details", "TPL6_DETAILS");
            convertListToString(aiData, finalData, "next_plan", "TPL6_NEXT_PLAN");
            convertListToString(aiData, finalData, "etc", "TPL6_ETC");
            break;
            
        case "업무일지양식7.docx":
            // 공사/작업 내역 등의 목록을 줄 바꿈 문자열로 변환하여 단일 필드에 삽입
            convertListToString(aiData, finalData, "today_work_detail", "TPL7_TODAY_WORK_DETAIL");
            convertListToString(aiData, finalData, "personnel_count", "TPL7_PERSONNEL_COUNT");
            convertListToString(aiData, finalData, "equipment_detail", "TPL7_EQUIPMENT_DETAIL");
            convertListToString(aiData, finalData, "material_detail", "TPL7_MATERIAL_DETAIL");
            convertListToString(aiData, finalData, "other_notes", "TPL7_OTHER_NOTES");
            
            convertListToString(aiData, finalData, "next_plan_detail", "TPL7_NEXT_PLAN_DETAIL");
            convertListToString(aiData, finalData, "next_personnel_plan", "TPL7_NEXT_PERSONNEL_PLAN");
            convertListToString(aiData, finalData, "next_equipment_plan", "TPL7_NEXT_EQUIPMENT_PLAN");
            convertListToString(aiData, finalData, "next_material_plan", "TPL7_NEXT_MATERIAL_PLAN");
            convertListToString(aiData, finalData, "next_other_notes", "TPL7_NEXT_OTHER_NOTES");
            break;
            
        // 양식 1, 3 등은 DB 매핑만으로 처리가능하다는 가정 하에 별도 case가 없음.
    }
    	return finalData;
    }
    
    // 하나의 문자열로 변환하는 작업, 쉽게 리스트 항목을 줄글로 보기좋게 정리해서 옮겨담는 것임
    private void convertListToString(Map<String, Object> aiData, Map<String, Object> finalData, String aiKey, String tplKey) {
    	
    	Object value = aiData.get(aiKey);
    	// 리스트 형태인지 확인
    	if(value instanceof List) {
    		StringBuilder sb = new StringBuilder(); // 반복문 내 여러 요소 줄 바꿈 문자열을 하나로 합칠때 권장된다.
    		
    		//리스트를 줄 바꿈 문자열로 변환할거임
    		List<String> list = (List<String>) value;
    		
    		// 예) • 사과
    		for(int i = 0; i < list.size(); i++) {
    			sb.append("• ").append(list.get(i));
    			// 마지막 전까지만 줄바꿈 실행
    			if(i < list.size() - 1) {
    				sb.append("\n");
    			}
    		}
    		finalData.put(tplKey, sb.toString());
    	} 
    	
    }
}