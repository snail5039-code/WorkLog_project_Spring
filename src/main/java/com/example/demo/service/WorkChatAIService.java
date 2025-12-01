package com.example.demo.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Lombok의 로깅 어노테이션

@Service
@RequiredArgsConstructor
@Slf4j // 이 어노테이션이 'log' 객체를 자동으로 생성합니다.
public class WorkChatAIService {

	// final로 생성자 주입
	private final ChatClient chatClient;

}
