package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkLog {
	
//	private int id; 나중에 식별자로 자동 증가 할 것 
	
	private String regDate; 
	private String updateDate; 
	private String title; 
	private String mainContent; 
	private String sideContent; 
	
	private int memberId; //여기 두개는 나중에 그냥 다른 테이블에서 받아와도 되지 않을까 함
	private int boardId; 
	
}
