package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
// 여기는 그ㅡ냥 그거임 아이디, 비번 찾을때 좀 더 안헷갈리려고, 분리하기 귀찮으니 디티오는 합침
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyFindIdRequest {
	private String name;
	private String email;
	private String loginId;
	private String newPassword;
	private String confirmPassword;
	private String code;
}
