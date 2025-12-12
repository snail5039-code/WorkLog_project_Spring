package com.example.demo.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PageContent;
import com.example.demo.service.PageContentService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequestMapping("/api")
public class ChatController {
	// 챗봇 전용 컨트롤러임!
	
	private final PageContentService pageContentService;
	private final ChatClient chatClient;
	
	public ChatController(PageContentService pageContentService, ChatClient chatClient) {
		this.pageContentService = pageContentService;
		this.chatClient = chatClient;
	}
	
	// === 요청/응답용 DTO
    public static class ChatRequest {
        private String question;

        public String getQuestion() {
            return question;
        }
        public void setQuestion(String question) {
            this.question = question;
        }
    }

    public static class ChatResponse {
        private String answer;

        public ChatResponse(String answer) {
            this.answer = answer;
        }

        public String getAnswer() {
            return answer;
        }
        public void setAnswer(String answer) {
            this.answer = answer;
        }
    }
    
    @PostMapping("/chat")
    public ChatResponse chat(@RequestBody ChatRequest request) {
    	
    	String question = request.getQuestion();
    	if(question == null || question.isBlank()) {
    		return new ChatResponse("질문을 입력해 주세요.");
    	}
    	
    	// 페이지 내용 검색, candidates 후보자
    	List<PageContent> candidates =  pageContentService.searchByKeyword(question);
    	
    	// 페이지 내용으로 context 만들기 뼈대 만드는 거
    	StringBuilder contextBuilder = new StringBuilder();
    	
    	int maxPages = Math.min(3, candidates.size());
    	// 검색한 내용 페이지 파악 후 최대 3페이지까지만 흙고 합친다.
    	for (int i = 0; i < maxPages; i++) {
            PageContent p = candidates.get(i);
            contextBuilder.append("### 페이지: ")
                    .append(p.getTitle())
                    .append(" (").append(p.getUrl()).append(")\n");

            String content = p.getContent();
            if (content != null) {
                if (content.length() > 2000) {
                    content = content.substring(0, 2000) + "...";
                }
                contextBuilder.append(content).append("\n\n");
            }
        }
    	String contextText = contextBuilder.toString();
    	String safeContext = contextText.isBlank()
    	        ? "현재 참고할 수 있는 WorkLog 문서가 없습니다."
    	        : contextText;
    	String prompt =  """
    	        당신은 'WorkLog'라는 업무일지/인수인계 서비스의 전용 도우미이자,
				일상적인 대화와 일반적인 질문에도 답변할 수 있는 한국어 챗봇입니다.
				
				# 1. 모드
				
				- WorkLog 모드:
				  - 질문이 WorkLog 서비스, 업무일지, 인수인계, 템플릿, 마이페이지,
				    로그인/회원가입, 챗봇 사용법 등과 관련되어 있거나,
				    아래 사이트 문맥에 관련 내용이 있으면 이 모드로 답하세요.
				
				- 일반 대화 모드:
				  - 위와 무관한 일상 대화(잡담, 공부/개발 질문, 취업 고민 등)라면
				    보통의 AI 챗봇처럼 편하게 답해도 됩니다.
				
				# 2. 사이트 문맥 (WorkLog/블로그에서 크롤링한 내용)
				
				다음은 사용자가 직접 작성한 WorkLog 소개/이용 방법/양식 예시/FAQ 등 공식 문서입니다.
				WorkLog 관련 질문일 때는 이 문맥을 최우선으로 참고해서 답변하세요.
				
				---- 사이트 문맥 시작 ----
				%s
				---- 사이트 문맥 끝 ----
				
				# 3. 사용자 질문
				
				%s
				
				# 4. 응답 규칙
				
				1) 먼저 이 질문이 WorkLog 관련인지 판단하세요.
				   - '회원가입', '로그인', '업무일지', '인수인계', '주간/월간',
				     '양식', '템플릿', '마이페이지', '챗봇', 'WorkLog' 같은 단어가 들어가면
				     거의 WorkLog 관련입니다.
				   - 문맥 안에 비슷한 내용이 있다면 그것도 WorkLog 관련으로 봅니다.
				
				2) WorkLog 관련이면:
				   - 위의 사이트 문맥에서 최대한 근거를 찾아 답하세요.
				   - 문맥에 있는 표현과 예시를 활용해 구체적으로 설명합니다.
				   - 문맥에 없는 세부 화면 구조나 버튼 위치는 마음대로 지어내지 말고,
				     "서비스 화면에 따라 다를 수 있어요" 정도로만 안내하세요.
				
				3) 문맥에 해당 내용이 전혀 없지만,
				   - '좋은 업무일지', '인수인계 잘 쓰는 법'처럼 일반적인 개념 질문이면
				     일반적인 상식과 경험을 바탕으로 답하되,
				     "일반적인 기준"이라는 점을 한 번 밝혀 주세요.
				
				4) WorkLog와 상관없는 일상 대화/공부/개발 질문이면:
				   - 사이트 문맥은 무시해도 괜찮고,
				   - 평소 AI 챗봇처럼 자연스럽고 친절하게 답변하세요.
				
				5) 모른다고 해야 하는 경우:
				   - WorkLog의 아주 구체적인 정책, 실제 회사 내부 규정, 정확한 메뉴/버튼 이름 등
				     문맥에도 없고 추측해야 하는 정보는 지어내지 마세요.
				   - 그럴 때는 "현재 문서에 없는 내용이라 정확히 알 수는 없지만, 일반적으로는 ~"처럼 답하세요.
				
				6) 말투:
				   - 사용자는 한국어를 쓰고 있고, 반말/존댓말이 섞여 있습니다.
				   - 답변은 기본적으로 존댓말이지만, 너무 딱딱하지 않은 자연스러운 톤으로 답해주세요.
                """.formatted(safeContext, question);

        String answer = chatClient
                .prompt(prompt)
                .call()
                .content();

        return new ChatResponse(answer);
    }
    
}
