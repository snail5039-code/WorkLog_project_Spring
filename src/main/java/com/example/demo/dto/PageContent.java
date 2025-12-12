package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageContent {
	private int id;
	private String url;
    private String title;
    private String content;
    private LocalDateTime crawledAt;
	
}
