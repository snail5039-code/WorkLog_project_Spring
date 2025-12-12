package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PageContent;
import com.example.demo.service.PageContentService;

@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"}, allowCredentials = "true")
@RequestMapping("/api")
public class CrawlController {
	private final PageContentService pageContentService;
	
	public CrawlController(PageContentService pageContentService) {
		this.pageContentService = pageContentService;
	}
	
	// url 하나 받아서 크롤링 + 저장
    @PostMapping("/crawl")
    public ResponseEntity<?> crawl(@RequestParam String url) {
        try {
            PageContent saved = pageContentService.crawlAndSave(url);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("크롤링 실패: " + e.getMessage());
        }
    }
    
    @GetMapping("/crawl")
    public ResponseEntity<?> testCrawl(@RequestParam String url) {
        try {
            PageContent saved = pageContentService.crawlAndSave(url);
            return ResponseEntity.ok(saved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("크롤링 실패: " + e.getMessage());
        }
    }
}
