package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.dto.PageContent;

@Mapper
public interface PageContentDao {
	
	// INSERT
    @Insert("""
        INSERT INTO pageContent (url, title, content, crawled_at)
    		VALUES (#{url}, #{title}, #{content}, #{crawledAt})
        """)
    void insert(PageContent page);

	@Select("""
		    SELECT id, url, title, content, crawled_at
			   	FROM pageContent
				WHERE url = #{url}
		     """)
	PageContent findByUrl(String url);

	@Select("""
			SELECT id, url, title, content, crawled_at
			     FROM pageContent
			     WHERE title LIKE CONCAT('%', #{question}, '%')
			        OR content LIKE CONCAT('%', #{question}, '%')
			     ORDER BY crawled_at DESC
			""")
	List<PageContent> searchByKeyword(String question);

	@Update("""
			UPDATE pageContent
			SET
			    title = #{title},
			    content = #{content},
			    crawled_at = #{crawledAt}
			WHERE url = #{url}
			""")
	void update(PageContent page);

	// 전체 조회
	@Select("""
			SELECT id, url, title, content, crawled_at
				FROM pageContent
				ORDER BY crawled_at DESC
			""")
	List<PageContent> findAll();
	
	


}
