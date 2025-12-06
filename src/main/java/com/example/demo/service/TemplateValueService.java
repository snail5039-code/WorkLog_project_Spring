package com.example.demo.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class TemplateValueService {
	
	private final ObjectMapper objectMapper;

    public TemplateValueService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

	// 플레이스 홀더 안에 있는거를 치환해주는 것!
	public Map<String, String> buildValuesFromRaw(Map<String, Object> raw) {
		Map<String, String> values = new HashMap<>();
		
		for(Map.Entry<String, Object> e : raw.entrySet()) {
			String key = e.getKey();
			Object v = e.getValue();
			String placeholder = "${" + key + "}";
			
			values.put(placeholder, v != null ? v.toString() : "");
		}
		return values;
	}
	// 쉽게 ai가 요약한 거를 Map<String, Object>으로 자바에서 쓸수 있게 바꿔서 템플릿에 넣기 좋게 만드는 것임
	public Map<String, String> buildValuesFromJson(String json) throws IOException {
		Map<String, Object> raw = objectMapper.readValue(
				json, new TypeReference<Map<String, Object>>() {}
		);
		return buildValuesFromRaw(raw);
	}
}
