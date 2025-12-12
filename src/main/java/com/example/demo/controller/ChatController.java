package com.example.demo.controller;

import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    	if(contextText.isBlank()) {
    		contextText = "관련된 페이지 내용이 없습니다.";
    	}
    	String prompt =  """
    	        너는 이 웹서비스의 **전용 도우미 챗봇**이다.

    	        아래에는 이 사이트에서 수집한 페이지들의 일부 내용이 주어진다.
    	        각 페이지는 `### 페이지: {제목} ({URL})` 형식의 헤더와
    	        그 아래에 본문 텍스트가 붙어 있다.

    	        [사이트 페이지 내용]
    	        %s

    	        [사용자 질문]
    	        %s

    	        답변을 만들 때 다음 지침을 반드시 지켜라:

    	        1. 우선 위에 주어진 페이지 내용 안에서 답을 찾으려고 노력하라.
    	        2. 답을 찾을 수 있다면,
    	           - 자연스러운 한국어로 정리해서 설명하라.
    	           - 가능하면 어떤 페이지(제목)를 참고했는지도 함께 언급하라.
    	             예: "FAQ 페이지 기준으로 말씀드리면..." 처럼.
    	        3. 여러 페이지의 정보가 섞여 있다면,
    	           - 서로 모순되지 않도록 내용을 잘 통합해서 하나의 답변으로 정리하라.
    	        4. 페이지 내용에 **명시적으로 없는 정보는 지어내지 마라.**
    	           - 정말로 알 수 없으면
    	             "이 사이트에서 제공하는 정보만으로는 정확한 답을 찾을 수 없습니다."
    	             라고 말하고,
    	             사용자가 어디에서 더 확인해야 할지(예: 관리자 문의, 공지사항)를 간단히 안내하라.
    	        5. 불필요하게 장황하게 늘어놓지 말고,
    	           - 핵심만 간결하게 설명하되,
    	           - 필요한 경우 목록/번호 등을 사용해 보기 좋게 정리하라.

    	        반드시 이 사이트의 페이지 내용을 **최우선**으로 사용하고,
    	        사이트 정보와 명확히 충돌하는 일반 상식은 사용하지 마라.
                """.formatted(contextText, question);

        String answer = chatClient
                .prompt(prompt)
                .call()
                .content();

        return new ChatResponse(answer);
    }
    
}
