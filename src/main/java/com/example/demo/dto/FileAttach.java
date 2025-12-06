package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileAttach {
	
	private int id;
	private String regDate;
	private String updateDate;
	private int workLogId;
	private String fileName;
	private String filePath;
	private long fileSize;
}