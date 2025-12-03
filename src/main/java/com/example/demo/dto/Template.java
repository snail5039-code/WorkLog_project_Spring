package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Template {
	private int id;
	private String templateFileName;
	private String jsonKey;
	private String placeholder;
}
