package com.example.demo.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class TemplateMetaService {
	
    public static class Field {
        private final String key;
        private final String example;

        public Field(String key, String example) {
            this.key = key;
            this.example = example;
        }

        public String getKey() { return key; }
        public String getExample() { return example; }
    }

    // 템플릿별 필드 목록
    private final Map<String, List<Field>> templateFields = new HashMap<>();
    
    public TemplateMetaService() {
        // 🔹 TPL1 등록 (지금 쓰는 주간 업무일지)
        templateFields.put("TPL1", List.of(
                new Field("TPL1_DATE", "yyyy-MM-dd 형식의 날짜"),
                new Field("TPL1_MON_TASK_TODAY", "월요일 오늘 한 일"),
                new Field("TPL1_MON_TASK_NEXT", "월요일 내일 할 일"),
                new Field("TPL1_TUE_TASK_TODAY", "화요일 오늘 한 일"),
                new Field("TPL1_TUE_TASK_NEXT", "화요일 내일 할 일"),
                new Field("TPL1_WED_TASK_TODAY", "수요일 오늘 한 일"),
                new Field("TPL1_WED_TASK_NEXT", "수요일 내일 할 일"),
                new Field("TPL1_THU_TASK_TODAY", "목요일 오늘 한 일"),
                new Field("TPL1_THU_TASK_NEXT", "목요일 내일 할 일"),
                new Field("TPL1_FRI_TASK_TODAY", "금요일 오늘 한 일"),
                new Field("TPL1_FRI_TASK_NEXT", "금요일 내일 할 일"),
                new Field("TPL1_SAT_TASK_TODAY", "토요일 오늘 한 일"),
                new Field("TPL1_SAT_TASK_NEXT", "토요일 내일 할 일"),
                new Field("TPL1_SUGGESTIONS", "특이사항/건의사항 요약")
        ));
        
        // 🔹 TPL3 : 간단 일일 업무일지 (업무일지양식3.docx)
        templateFields.put("TPL3", List.of(
                new Field("TPL3_DATE", "작성일자 (yyyy-MM-dd)"),
                new Field("TPL3_AUTHOR", "작성자 이름 (예: 홍길동)"),
                new Field("TPL3_CONTENT", "오늘 수행한 업무 내용 전체"),
                new Field("TPL3_NOTE", "비고 / 특이사항"),
                new Field("TPL3_SUMMARY", "한두 문장으로 요약한 오늘 업무 요약")
        ));

        // 🔹 TPL4 : 부서/작성자/오늘&다음계획 타입 (업무일지양식4.docx)
        templateFields.put("TPL4", List.of(
                new Field("TPL4_REPORT_ID", "보고서 번호 또는 제목 (예: 2025-12-1차 주간보고)"),
                new Field("TPL4_DATE", "작성일자 (yyyy-MM-dd)"),
                new Field("TPL4_DEPT", "부서명 (예: 개발팀)"),
                new Field("TPL4_AUTHOR", "작성자 이름"),
                new Field("TPL4_TODAY_TASKS", "오늘 수행한 주요 업무 요약"),
                new Field("TPL4_NEXT_PLAN", "다음 작업/계획 요약"),
                new Field("TPL4_NOTE", "기타 특이사항, 요청사항")
        ));

        // 🔹 TPL5 : 업무 리스트형(프로젝트/담당자/상태) (업무일지양식5.docx)
        templateFields.put("TPL5", List.of(
        		new Field("TPL5_DATE", "작성일자 (yyyy-MM-dd)"),
                new Field("TPL5_POSITION", "직급 (예: 대리, 과장 등)"),
                new Field("TPL5_AUTHOR", "작성자 이름 (예: 홍길동)"),
                new Field("TPL5_TASK_STATUS", "진행상황 (예: 완료, 진행중, 보류 등)"),
                new Field("TPL5_TASK_PRIORITY", "우선순위 (예: 상, 중, 하)"),
                new Field("TPL5_TASK_CATEGORY", "업무 분류 (예: 개발, 회의, 문서작성 등)"),
                new Field("TPL5_TASK_DETAIL", "세부내역 및 상관 업무 내용"),
                new Field("TPL5_TASK_NOTE", "비고 / 특이사항")
        ));

        // 🔹 TPL6 : 오늘 업무/이슈/내일 계획 타입 (업무일지양식6.docx)
        templateFields.put("TPL6", List.of(
                new Field("TPL6_DATE", "작성일자 (yyyy-MM-dd)"),
                new Field("TPL6_TEAM", "팀/부서명 (예: 백엔드팀)"),
                new Field("TPL6_AUTHOR", "작성자 이름"),
                new Field("TPL6_TODAY_TASKS", "오늘 수행한 주요 업무 요약"),
                new Field("TPL6_TODAY_ISSUE", "오늘 발생한 이슈 및 문제점"),
                new Field("TPL6_NEXT_PLAN", "다음 작업/내일 계획"),
                new Field("TPL6_DETAILS", "세부 내용 또는 추가 설명"),
                new Field("TPL6_ETC", "기타 특이사항, 요청사항")
        ));

        // 🔹 TPL7 : 프로젝트 / 오늘 상세 / 다음 계획(인력·장비·자재) (업무일지양식7.docx)
        templateFields.put("TPL7", List.of(
                new Field("TPL7_PROJECT_NAME", "프로젝트명 또는 공사명"),
                new Field("TPL7_TASK_NOTE", "오늘의 작업 제목 또는 한줄 설명"),
                new Field("TPL7_TODAY_WORK_DETAIL", "오늘 수행한 작업 상세 내용"),
                new Field("TPL7_OTHER_NOTES", "현장 특이사항, 안전 관련 메모 등"),
                new Field("TPL7_PERSONNEL_COUNT", "투입 인원 수 (예: 5명)"),
                new Field("TPL7_EQUIPMENT_DETAIL", "사용 장비 및 장비 관련 내용"),
                new Field("TPL7_MATERIAL_DETAIL", "사용 자재/소모품 관련 내용"),
                new Field("TPL7_NEXT_PLAN_DETAIL", "다음 작업 계획 요약"),
                new Field("TPL7_NEXT_PERSONNEL_PLAN", "다음 작업에 필요한 인력 계획"),
                new Field("TPL7_NEXT_EQUIPMENT_PLAN", "다음 작업에 필요한 장비 계획"),
                new Field("TPL7_NEXT_MATERIAL_PLAN", "다음 작업에 필요한 자재/소모품 계획"),
                new Field("TPL7_NEXT_OTHER_NOTES", "기타 다음 작업 관련 메모")
        ));

        // 🔹 나중에 TPL2, TPL3, ... 생기면 여기만 추가하면 됨
        // templateFields.put("TPL2", List.of(
        //         new Field("TPL2_DATE", "yyyy-MM-dd"),
        //         new Field("TPL2_MONTH_SUMMARY", "한 달 전체 업무 요약"),
        //         ...
        // )); 더 추가하면 됌
    }
    // 템플릿이 없으면 기본 값으로 1로 하라는 것
    public List<Field> getFields(String templateId) {
    	String id = (templateId == null ? "TPL1" : templateId.toUpperCase());
    	return templateFields.getOrDefault(id, templateFields.get("TPL1"));
    }
    
    /** AI에게 보여줄 JSON 예시 문자열 만들기 "title": "업무일지 제목 예시" 느낌으로 변환 */ 
    public String buildJsonExample(String templateId) {
        List<Field> fields = getFields(templateId);
        String body = fields.stream()
                .map(f -> "  \"" + f.getKey() + "\": \"" + f.getExample() + "\"")
                .collect(Collectors.joining(",\n"));

        return "{\n" + body + "\n}";
    }
    
    /** 공통 규칙 + 템플릿별 JSON 예시를 합쳐서 최종 systemPrompt 생성 */
    public String buildSystemPrompt(String templateId) {
        String jsonExample = buildJsonExample(templateId);

        return """
                당신은 업무일지 요약을 만드는 도우미입니다.

                아래 형식과 완전히 동일한 JSON 객체만 반환해야 합니다.
                키 이름은 절대 바꾸지 말고, 키를 추가/삭제하지 마세요.

                이 템플릿의 예시 형식:
                %s

                규칙:
                1. 위 예시 JSON에 나오는 키들만 사용하고, 키 이름을 절대 바꾸지 마세요.
                2. 값은 한국어 한두 문장으로 채우세요.
                3. 응답은 ``` 같은 마크다운을 포함하지 말고, 순수 JSON 문자열만 반환하세요.
                4. 응답의 첫 글자는 반드시 '{', 마지막 글자는 반드시 '}' 여야 합니다.
                """.formatted(jsonExample);
    }
}
