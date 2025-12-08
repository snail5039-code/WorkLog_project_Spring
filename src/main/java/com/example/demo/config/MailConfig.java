//package com.example.demo.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//
//@Configuration
//public class MailConfig {
//	
//	@Bean
//    public JavaMailSender javaMailSender() {
//        // 일단 기본 객체만 만들어주기
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//
//        // 나중에 여기서 host, port, username, password 셋팅할 거야
//        // mailSender.setHost(...);
//        // mailSender.setPort(...);
//        // mailSender.setUsername(...);
//        // mailSender.setPassword(...);
//
//        return mailSender;
//    }
//}
