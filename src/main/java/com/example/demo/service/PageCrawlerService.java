package com.example.demo.service;

import java.io.IOException;
import java.time.LocalDateTime;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;

import com.example.demo.dto.PageContent;

@Service
public class PageCrawlerService {
	
	public PageContent crawl(String url) throws IOException {
		 // 1) 겉 페이지 먼저 가져오기
	    Document doc = Jsoup.connect(url)
	            .userAgent("Mozilla/5.0")
	            .get();
		 // 2) 네이버 블로그면 mainFrame 안쪽으로 한 번 더 들어가기
	    if (url.contains("blog.naver.com")) {
	        Element frame = doc.selectFirst("frame#mainFrame, iframe#mainFrame");
	        if (frame != null) {
	            // absUrl("src") 쓰면 상대경로도 전체 URL로 바꿔줌
	            String frameUrl = frame.absUrl("src");
	            if (frameUrl != null && !frameUrl.isBlank()) {
	                doc = Jsoup.connect(frameUrl)
	                        .userAgent("Mozilla/5.0")
	                        .get();
	            }
	        }
	    }

	    // 3) 제목 뽑기
	    String title = doc.title();
	    if (title == null || title.isBlank()) {
	        // og:title 메타 태그도 한 번 시도
	        Element ogTitle = doc.selectFirst("meta[property=og:title]");
	        if (ogTitle != null) {
	            title = ogTitle.attr("content");
	        }
	    }
	    if (title == null || title.isBlank()) {
	        title = url;
	    }

	    // 4) 본문 텍스트 뽑기
	    // 네이버 블로그 에디터 영역이 보통 이쪽에 있음
	    String bodyText = doc.select("#postViewArea, .se-main-container").text();
	    if (bodyText == null || bodyText.isBlank()) {
	        bodyText = doc.body() != null ? doc.body().text() : "";
	    }

	    PageContent page = new PageContent();
	    page.setUrl(url);
	    page.setTitle(title);
	    page.setContent(bodyText);
	    page.setCrawledAt(LocalDateTime.now());

	    return page;
	}
}
