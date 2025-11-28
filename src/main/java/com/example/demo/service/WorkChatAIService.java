package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Lombok의 로깅 어노테이션

@Service
@RequiredArgsConstructor
@Slf4j // 이 어노테이션이 'log' 객체를 자동으로 생성합니다.
public class WorkChatAIService {

    // @Slf4j를 사용하므로 수동 로거 선언은 모두 제거되었습니다.
    
    // final로 생성자 주입
    private final ChatClient chatClient;  

    public String summarizeWorkLog(String workContent, String formatGuideline) {
        // @Slf4j로 생성된 log 객체를 사용
        log.info("AI 요약 서비스 시작.");
        log.debug("입력된 업무 내용 길이: {}", workContent.length());
        log.debug("입력된 양식: {}", formatGuideline);

        // AI에게 보낼 최종 프롬프트를 구성합니다.
        // 이 프롬프트가 AI의 응답 형태를 결정합니다.
        String prompt = String.format(
                """
                당신은 전문적인 업무일지 요약 전문가입니다.
                다음 '업무 내용'을 분석하여, 아래 '업무일지 양식'에 맞춰 내용을 요약하고 재구성하여 **결과만** 반환하세요.
                제목이나 추가 설명 없이, 오직 양식에 따른 요약 내용만 마크다운 형식으로 한국어로 출력해야 합니다.
                
                **[추가 지침]**
                1. '업무 내용'에 '오늘 목표'가 명시적으로 없는 경우, **'주요 성과'를 바탕으로 가장 합리적인 목표를 추론하여** '오늘 목표' 섹션을 채우십시오.
                2. 만약 내용이 전혀 없더라도 양식의 모든 항목을 채워야 합니다.
                
                --- 업무일지 양식 ---
                %s
                ----------------------

                --- 업무 내용 ---
                %s
                ----------------
                """, 
                formatGuideline, 
                workContent
            );
        
        // 💡 ChatClient를 사용하여 AI 모델에 요청을 보내고 응답을 받습니다.
        // 요거는 그냥 명령을 사용자 입력으로 전달, 모델 호출, 응답 내용 추출 이런느낌임 
        String aiResponse = "";
        try {
            aiResponse = chatClient.prompt()
                .user(prompt)
                .call()
                .content();
            
            log.info("AI 응답 성공. 내용: {}", aiResponse.trim());
        } catch (Exception e) {
            // 💡 ChatClient 통신 자체에서 예외 발생 시 로그 기록
            log.error("AI ChatClient 통신 중 심각한 오류 발생:", e);
            // 🚨 DB에 오류 메시지를 저장하여 어떤 통신 오류인지 확인합니다.
            return "AI 통신 오류 발생: " + e.getMessage(); 
        }

        // 불필요한 공백 제거 후 반환
        return aiResponse.trim();
    }
}