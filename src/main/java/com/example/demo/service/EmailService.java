package com.example.demo.service;

import java.security.SecureRandom;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
	
	private final JavaMailSender mailSender;
	private final SecureRandom random = new SecureRandom();
	
	public EmailService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}
	
	// 6자리 숫자 인증코드 만들기, 생성확인코드
	public String generateVerificationCode() {
		int code = 100000 + random.nextInt(900000); // 100000 ~ 999999 사이 숫자 생성
		return String.valueOf(code);
	}
	
	// 실제로 메일 보내기 
	public void sendMail(String to, String subject, String text) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(to);         // 받는 사람
        message.setSubject(subject); // 제목
        message.setText(text);       // 내용
        mailSender.send(message);
	}
}
