package com.example.demo.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PageContent;

@Service
public class PageCrawlerService {
	
	public PageContent crawl(String url) throws IOException {
		
		Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").get();
		
		// 2) 제목, 본문 텍스트 뽑기
        String title = doc.title();           // <title>...</title>
        String bodyText = doc.body().text();  // HTML 전체에서 텍스트만 추출

        // 3) PageContent DTO에 담기
        PageContent page = new PageContent();
        page.setUrl(url);
        page.setTitle(title != null ? title : url);  // 제목 없으면 url로 대체
        page.setContent(bodyText);
        page.setCrawledAt(LocalDateTime.now());

        return page;
	}
}
