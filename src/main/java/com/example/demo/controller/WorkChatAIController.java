//package com.example.demo.controller;
//
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.example.demo.dto.ChatRequest;
//import com.example.demo.service.WorkChatAIService;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//@CrossOrigin(origins = "http://localhost:5173") // React 개발 서버 포트 확인
//public class WorkChatAIController {
//	private final WorkChatAIService workChatAIService;
//	
//	@PostMapping("/chat") 
//    public String handleChatRequest(@RequestBody ChatRequest request) {
//        
//        // 1. 클라이언트에서 받은 긴 질문 내용을 추출
//        String questionContent = request.getQuestion();
//        
//        System.out.println("AI 요청 내용 수신: " + questionContent.substring(0, Math.min(questionContent.length(), 50)) + "...");
//        
//        // 2. 💡 수정: workChatAIService의 요약 메서드를 호출하여 실제 AI 결과를 받습니다.
//        try {
//            String aiResult = workChatAIService.summarizeWorkLog(questionContent);
//            
//            // 3. AI 결과를 클라이언트에 반환
//            return aiResult;
//        } catch (Exception e) {
//            // AI 호출(Ollama 서버 접속 등) 중 오류가 발생하면 에러 메시지를 반환합니다.
//            System.err.println("AI 서비스 호출 중 오류 발생: " + e.getMessage());
//            return "요약 실패: AI 서비스 호출 중 서버 오류 발생"; 
//        }
//	}
//}
//
//// 이 페이지도 추후 사용 혹시나 실시간으로 할일 있으면 쓴다