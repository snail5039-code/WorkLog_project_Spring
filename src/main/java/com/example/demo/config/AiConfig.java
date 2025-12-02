package com.example.demo.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel; // ✅ OpenAiChatModel을 사용합니다.
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

	/**
	 * ChatClient 빈을 수동으로 등록합니다.
	 * OpenAiChatModel이 자동 구성되었다고 가정하고 이를 사용하여 ChatClient를 생성합니다.
	 * @param openAiChatModel Spring Context에서 찾은 OpenAiChatModel 빈
	 * @return ChatClient 인스턴스
	 */
	@Bean
	public ChatClient chatClient(OpenAiChatModel openAiChatModel) {
		// OpenAiChatModel을 사용하여 ChatClient를 빌드합니다.
		return ChatClient.builder(openAiChatModel).build();
	}
}

// 이 페이지도 추후 사용 