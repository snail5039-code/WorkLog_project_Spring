package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandoverLog {
	private int id;
	private String regDate;
	private String updateDate;
	private int memberId;
	private String writerName;
	private String title;
    private String toName;
    private String toJob;
    private String fromJob;

    private String fromDate;  // "2025-12-01" 이런 형식
    private String toDate;    // "2025-12-10"
    private String content;  
}
