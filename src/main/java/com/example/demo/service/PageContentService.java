package com.example.demo.service;

import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dao.PageContentDao;
import com.example.demo.dto.PageContent;


@Service
public class PageContentService {

	private final PageContentDao pageContentDao;
	private final PageCrawlerService crawlerService;
	
	public PageContentService(PageContentDao pageContentDao, PageCrawlerService crawlerService) {
		this.pageContentDao = pageContentDao;
		this.crawlerService = crawlerService;
	}
	
	public PageContent crawlAndSave(String url) throws IOException {
		// 1. 먼저 Jsoup으로 해당 URL 페이지 긁어오기
        PageContent crawled = crawlerService.crawl(url);
        
     // 2. 이 URL이 DB에 이미 있는지 확인
        PageContent existing = pageContentDao.findByUrl(url);
        
        if (existing == null) {
            // 2-1. 없으면 새로 INSERT
            pageContentDao.insert(crawled);
            return crawled;
        } else {
            // 2-2. 있으면 내용만 업데이트
            existing.setTitle(crawled.getTitle());
            existing.setContent(crawled.getContent());
            existing.setCrawledAt(crawled.getCrawledAt());

            pageContentDao.update(existing);
            return existing;
        }
	}

	public List<PageContent> searchByKeyword(String question) {
		return this.pageContentDao.searchByKeyword(question);
	}

}
