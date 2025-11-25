package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.OllamaChatModel; // OllamaChatModel을 사용합니다.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    /**
     * ChatClient 빈을 수동으로 등록합니다.
     * OllamaChatModel이 자동 구성되었다고 가정하고 이를 사용하여 ChatClient를 생성합니다.
     * * @param ollamaChatModel Spring Context에서 찾은 OllamaChatModel 빈
     * @return ChatClient 인스턴스
     */
    @Bean
    public ChatClient chatClient(OllamaChatModel ollamaChatModel) {
        return ChatClient.builder(ollamaChatModel).build();
    }
}

// 이 페이지도 추후 사용 