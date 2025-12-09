package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FirebaseLoginRequest {
	private String idToken;   // 프론트에서 받은 Firebase ID 토큰
    private String provider; 
}
