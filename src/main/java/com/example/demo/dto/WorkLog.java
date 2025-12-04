package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkLog {
	
	private int id; 
	private String regDate; 
	private String updateDate; 
	private String title; 
	private String mainContent; 
	private String sideContent; 
	private String writerName; 
	private String summaryContent; 
	
	private int memberId; //여기 두개는 나중에 그냥 다른 테이블에서 받아와도 되지 않을까 함
	private int boardId; 
    private String templateType; 
	private List<FileAttach> fileAttaches; //받아온 값 보여주려면 이거 있어야됌
	
}
