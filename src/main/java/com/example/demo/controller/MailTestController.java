package com.example.demo.controller;

import com.example.demo.service.EmailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mail")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class MailTestController {

    private final EmailService emailService;

    public MailTestController(EmailService emailService) {
        this.emailService = emailService;
    }

    // 예: GET http://localhost:8080/api/mail/test?to=네이메일주소
    @GetMapping("/test")
    public String sendTestMail(@RequestParam String to) {
        String code = emailService.generateVerificationCode();
        String subject = "[업무일지 서비스] 메일 테스트";
        String text = "이메일 발송 테스트입니다.\n테스트 인증코드: " + code;

        emailService.sendMail(to, subject, text);

        return "메일 전송 완료: " + to;
    }
}
