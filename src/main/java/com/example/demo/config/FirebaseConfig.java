// src/main/java/com/example/demo/config/FirebaseConfig.java
package com.example.demo.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() throws IOException {
        // 이미 초기화된 게 없을 때만
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(
                                    new ClassPathResource("firebase/serviceAccountKey.json")
                                            .getInputStream()
                            )
                    )
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println(">>> Firebase App 초기화 완료");
        }
    }
}
