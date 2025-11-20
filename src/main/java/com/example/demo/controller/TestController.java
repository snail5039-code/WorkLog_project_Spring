package com.example.demo.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
	@GetMapping("/test")
	public List<String> hello() {
		String[] strArr = {"seoul", "incheon", "bucheon", "suwon", "ansan"};
		List<String> test = Arrays.asList(strArr);
		return test;
	}
}