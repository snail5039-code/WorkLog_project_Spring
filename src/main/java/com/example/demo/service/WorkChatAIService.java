package com.example.demo.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.WorkLogDao;
import com.example.demo.dto.Template;
import com.example.demo.dto.WorkLog;
import com.example.demo.util.FileTextExtractor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WorkChatAIService {
	@Value("${file.upload-dir}")
	private String uploadDir;
	// final로 생성자 주입
	private final ChatClient chatClient;
	private final FileTextExtractor fileTextExtractor;
	private final ObjectMapper objectMapper;
	private final WorkLogDao workLogDao;

	// AI는 빌더로 주입
	public WorkChatAIService(ChatClient.Builder chatClientBuilder, FileTextExtractor fileTextExtractor,
			WorkLogDao workLogDao) {
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

		String templateFilName = templateFile.getOriginalFilename();
		// 템플릿 기반 제이슨 스키마 자동생성
		Map<String, Object> schema = buildSchemaFromTemplate(templateFilName);

		System.out.println("DEBUG: Generated Schema = " + schema);
		// 제이슨 형식으로 변환해서 변수에 넣는거
		String schemaJson = objectMapper.writeValueAsString(schema);
		// 1. AI 응답을 받고 변수에 저장합니다.
		String rawAiResponse = chatClient.prompt().system("""
				    당신은 문서 자동 생성 전문가입니다.
				          아래 제공된 JSON 스키마를 **절대 변경하지 말고**, 그 형식대로만 응답하십시오.

				          ⚠ 반드시 스키마의 모든 필드를 포함해야 합니다.
				          ⚠ JSON 외의 텍스트는 절대 포함하면 안 됩니다.
				""").user("### JSON 스키마\n" + schemaJson + "\n\n### 새 업무 내용\n" + newContent).call().content();


		// 2. ⭐️ [강력 보강 로직] JSON 추출 로직

		// A. 먼저 마크다운 백틱(`)만 제거합니다.
		String cleanJson = rawAiResponse.trim().replaceAll("```json|```", "").trim();

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
		// 1) 템플릿 파일명 정규화 (확장자 제거)
	    String baseName = templateFileName.replaceAll("\\.(?i)docx$", "");

	    // 2) (1), (2) 같은 번호 제거
	    baseName = baseName.replaceAll("\\s*\\(\\d+\\)$", "");

	    // 3) _이후 텍스트 제거 (예: _복사본)
	    baseName = baseName.split("_")[0];

	    // 4) 다시 .docx 붙여서 DB 검색 가능하게 함
	    String normalizedFileName = baseName + ".docx";

	    System.out.println("정규화된 템플릿 파일명 = " + normalizedFileName);

	    // ⭐ 핵심 변경: LIKE 검색
	    List<Template> mappings = this.workLogDao.selectMappingsByFileNameLike(baseName + "%");
		// 최종적으로 전달할 데이터임
		Map<String, Object> finalData = new HashMap<>();

		// 안에 있는 1:1 매칭 되있는 값을 데이터 변환
		for (Template mapping : mappings) {
			String jsonKey = mapping.getJsonKey();
			String tplKey = mapping.getPlaceholder();

			Object value = aiData.get(jsonKey);

			if (value == null)
				continue;

			// 리스트면 보기좋게 문자열로 변환, 줄바꿈은 가독성을 위해 밑에 저거는 문법이니 그렇게 외우면됌
			// ?ㄴㄴ 어떤 값이든 처리하려고
			if (value instanceof List<?>) {
				List<?> list = (List<?>) value;
				String joined = list.stream().map(Object::toString).reduce((a, b) -> a + "\n" + b).orElse("");
				finalData.put(tplKey, joined);
			} else {
				finalData.put(tplKey, value.toString());
			}
		}

		return finalData;
	}

	// 데이터 처리하는 것
	public Map<String, Object> generateAndMapDocxData(WorkLog workLog, MultipartFile templateFile) throws Exception {

		if (templateFile == null || templateFile.isEmpty()) {
			throw new IllegalArgumentException("Docx 템플릿 파일이 유효하지 않습니다.");
		}

		// 1. AI를 통한 최종 보고서 내용 JSON 생성
		String rawName = templateFile.getOriginalFilename();
		String templateFileName = rawName.split("_")[0];
		templateFileName = templateFileName + ".docx";
		// DB 저장 시 NOT NULL 제약조건을 만족시키기 위함입니다.
		workLog.setDocumentType(templateFileName);

		// AI에게 전달할 사용자의 입력 내용 조합
		String newContent = String.format("주요 업무: %s\n\n보조 내용: %s", workLog.getMainContent(), workLog.getSideContent());

		// 최종 보고서 제이슨
		String finalReportJson = this.generateFinalReport(templateFile, newContent);

		// 2. JSON 데이터를 Map<String, Object>으로 변환
		Map<String, Object> aiData;
		try {
			// AI가 반환한 JSON 문자열을 Java Map으로 파싱, 깔끔하게 정리한다는 느낌
			aiData = objectMapper.readValue(finalReportJson, new TypeReference<Map<String, Object>>() {
			});
		} catch (IOException e) {
			throw new RuntimeException("AI 응답 JSON 파싱 실패: " + e.getMessage(), e);
		}

		// 3. ⭐️ 중요: AI가 생성한 데이터를 WorkLog DTO에 반영 (DB 저장 및 Null 방지 로직) ⭐️

		// (1) SummaryContent 설정 (AI가 생성한 진짜 요약본)
		String finalSummary = null;
		if (aiData.containsKey("final_summary")) {
			Object summaryObj = aiData.get("final_summary");
			// 값이 null이 아닌지 확인하여 NullPointerException 방지
			if (summaryObj != null) {
				finalSummary = summaryObj.toString();
				workLog.setSummaryContent(finalSummary);
			}
		}
		// 🚨 [핵심 수정: Null 안전성 및 대체 로직] summaryContent가 설정되지 않았을 경우 처리
		if (workLog.getSummaryContent() == null || workLog.getSummaryContent().trim().isEmpty()
				|| "[AI 요약 내용 누락: AI 응답에 'final_summary' 필드가 없습니다.]".equals(workLog.getSummaryContent())) {

			// AI 응답 실패 시 DB 저장 오류를 막고, AI 실패를 명확히 알리는 메시지를 기본값으로 설정
			String fallbackSummary = "[AI 요약 내용 누락: AI 응답에 'final_summary' 필드가 없습니다.]";

			// 대체 1순위: AI가 정제한 본문 필드 내용을 찾습니다.
			Object contentObj = aiData.getOrDefault("업무 내용",
					aiData.getOrDefault("내용", aiData.getOrDefault("주요 업무", aiData.get("mainContent"))));

			// 대체 2순위: 제목 후보
			Object titleObj = aiData.getOrDefault("문서 제목", aiData.getOrDefault("제목", aiData.get("title")));

			if (contentObj != null && !contentObj.toString().trim().isEmpty()) {
				// 1순위 적용: AI가 정제한 본문 내용이 있다면 요약 내용으로 사용 (너무 길면 잘라냄)
				String contentStr = contentObj.toString();
				fallbackSummary = "[자동 대체] 주요 업무 내용: " + contentStr.substring(0, Math.min(contentStr.length(), 100))
						+ (contentStr.length() > 100 ? "..." : "");
			} else if (titleObj != null && !titleObj.toString().trim().isEmpty()) {
				// 2순위: 제목이라도 있다면 요약 내용으로 사용
				fallbackSummary = "[자동 대체] 제목: " + titleObj.toString();
			}

			workLog.setSummaryContent(fallbackSummary);
			finalSummary = workLog.getSummaryContent(); // finalSummary 변수도 업데이트
		}

		// (2) MainContent 설정 (Null 방지 및 본문 확보)
		// [수정된 로직] MainContent가 비어있을 때 값을 채우는 우선순위가 변경되었습니다.
		if (workLog.getMainContent() == null || workLog.getMainContent().trim().isEmpty()
				|| "null".equals(workLog.getMainContent())) {
			// 1순위: 템플릿의 본문 필드 ("업무 내용", "내용", "주요 업무", "mainContent")를 찾습니다.
			Object contentObj = aiData.getOrDefault("업무 내용",
					aiData.getOrDefault("내용", aiData.getOrDefault("주요 업무", aiData.get("mainContent"))));

			if (contentObj != null) {
				// 1순위 적용: 템플릿의 본문 내용 (AI가 정제한 상세 내용)
				workLog.setMainContent(contentObj.toString());
			} else if (finalSummary != null) {
				// 2순위 적용: 본문 필드를 못 찾았다면, 요약본이라도 넣는다. (JSON 덤프보다 낫습니다.)
				workLog.setMainContent(finalSummary);
			} else {
				// 3순위 적용: 정말 아무것도 없으면 JSON 전체 덤프 (DB 저장 오류 방지용 최후의 수단)
				workLog.setMainContent(finalReportJson);
			}
		}

		// (3) Title 설정 (Null 방지)
		if (workLog.getTitle() == null || workLog.getTitle().trim().isEmpty()) {
			Object titleObj = aiData.getOrDefault("문서 제목", aiData.getOrDefault("제목", aiData.get("title")));
			if (titleObj != null) {
				workLog.setTitle(titleObj.toString());
			} else {
				workLog.setTitle("AI 자동 생성 보고서");
			}
		}

		// 4. 템플릿 키(TPLx_KEY)와 JSON 키를 매핑하여 문서 생성 엔진에 전달할 최종 데이터 맵 생성
		Map<String, Object> docxData = this.mapDataForTemplate(templateFileName, aiData);
		// 5. ⭐️ [복구/추가된 로직] WorkLogService에서 DB 값을 참조하거나 Docx 생성에 필요한 핵심 필드들을 docxData
		// 맵에 강제로 넣어줍니다.
		// Docx 생성 엔진이 이 키들을 사용할 수 있도록 보장합니다.
		docxData.put("title", workLog.getTitle());
		docxData.put("mainContent", workLog.getMainContent());
		docxData.put("summaryContent", workLog.getSummaryContent());

		// 매핑 준비 완료
		// 5. Docx 파일 생성에 필요한 최종 데이터 맵 반환
		return docxData;
	}

	// 템플릿 매핑을 자동으로 시키기 위해 스키마 자동 생성을 만듬
	public Map<String, Object> buildSchemaFromTemplate(String templateFileName) {
		List<Template> mappings = workLogDao.selectMappingsByFileName(templateFileName);

		Map<String, Object> schema = new HashMap<>();

		// 필요한 제이슨 키 자동 생성
		for (Template t : mappings) {
			schema.put(t.getJsonKey(), "");
		}

		// 공통 템플릿 필드 추가
		schema.put("final_summary", ""); // AI 요약 필수
		schema.put("title", "");
		schema.put("mainContent", "");
		schema.put("summaryContent", "");

		return schema;
	}

	public String extractJsonOnly(String text) {
		if (text == null)
			return null;

		int start = text.indexOf("{");
		int end = text.lastIndexOf("}");

		if (start == -1 || end == -1 || end <= start) {
			return null;
		}

		return text.substring(start, end + 1);
	}

	// 이거 나중에 설명 봐야함
	public byte[] fillTemplate(MultipartFile templateFile, Map<String, String> data) throws Exception {
		XWPFDocument document = new XWPFDocument(templateFile.getInputStream());

		// 문단 치환
		for (XWPFParagraph p : document.getParagraphs()) {
			for (XWPFRun run : p.getRuns()) {
				String text = run.getText(0);
				if (text != null) {
					for (String key : data.keySet()) {
						if (text.contains(key)) {
							text = text.replace(key, data.get(key));
							run.setText(text, 0);
						}
					}
				}
			}
		}

		// 테이블 내부 텍스트 치환
		for (XWPFTable table : document.getTables()) {
			for (XWPFTableRow row : table.getRows()) {
				for (XWPFTableCell cell : row.getTableCells()) {
					for (XWPFParagraph p : cell.getParagraphs()) {
						for (XWPFRun run : p.getRuns()) {
							String text = run.getText(0);
							if (text != null) {
								for (String key : data.keySet()) {
									if (text.contains(key)) {
										text = text.replace(key, data.get(key));
										run.setText(text, 0);
									}
								}
							}
						}
					}
				}
			}
		}

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		document.write(baos);
		return baos.toByteArray();
	}

}
