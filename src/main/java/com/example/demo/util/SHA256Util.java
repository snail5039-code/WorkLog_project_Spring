package com.example.demo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA256Util {

	public static String encrypt(String text) {
		if(text == null) {
			return null;
		}
		
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] bytes = md.digest(text.getBytes());
			
			StringBuilder sb = new StringBuilder();
			for(byte b : bytes) {
				sb.append(String.format("%02x", b)); // 2자리 16진수 변환
			}
			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("SHA-256 알고리즘을 찾을 수 없습니다.", e);
		}
	}
}
