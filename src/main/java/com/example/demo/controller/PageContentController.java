package com.example.demo.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.PageContent;
import com.example.demo.service.PageContentService;

@RestController
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:5174" }, allowCredentials = "true")
@RequestMapping("/api")
public class PageContentController {

	private final PageContentService pageContentService;

	public PageContentController(PageContentService pageContentService) {
		this.pageContentService = pageContentService;
	}
	
    // 1) 전체 크롤된 페이지 목록 조회
    @GetMapping("/pages")
    public List<PageContent> listAllPages() {
        return pageContentService.searchByKeyword(null); // null → findAll() 호출
    }

    // 2) 키워드로 검색된 페이지 목록 조회 (선택용)
    @GetMapping("/pages/search")
    public List<PageContent> searchPages(@RequestParam String keyword) {
        return pageContentService.searchByKeyword(keyword);
    }
}
