package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RePly {
	private int id;
	private String regDate;
	private String updateDate;
	private int workLogId;
	private int memberId;
	private String content;
	private String writerName;
}
