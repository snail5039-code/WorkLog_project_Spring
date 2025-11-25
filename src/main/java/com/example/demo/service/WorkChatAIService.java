package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkChatAIService {

	private final ChatClient chatClient; 

    public String summarizeWorkLog(String workContent) {
        // AI에게 보낼 최종 프롬프트를 구성합니다.
        // 이 프롬프트가 AI의 응답 형태를 결정합니다.
        String prompt = String.format(
            "다음 업무 내용을 분석하여 50자 이내의 **긍정적이고 매력적인 제목** 하나만 반환해줘. 제목 외의 다른 설명은 절대 추가하지 마세요. "
            + "아니 무조건 한국어로 답변해달라고 밑에 영어는 필요가 없어 회사 업무에 쓸건데 그렇게 하면 안되지!!!!: %s", 
            workContent
        );

        // 💡 ChatClient를 사용하여 AI 모델에 요청을 보내고 응답을 받습니다.
        String aiResponse = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

        // 불필요한 공백 제거 후 반환
        return aiResponse.trim();
    }
}

// 이 페이지는 추후 사용 