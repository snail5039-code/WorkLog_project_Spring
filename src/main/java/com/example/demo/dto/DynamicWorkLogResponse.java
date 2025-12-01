package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DynamicWorkLogResponse {
	
	//뼈대임!
	private List<String> headers;
	
	//본문 내용
	private String summaryJsonData;
}
