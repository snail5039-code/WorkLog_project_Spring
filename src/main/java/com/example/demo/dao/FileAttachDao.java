package com.example.demo.dao;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

import com.example.demo.dto.FileAttach;

@Mapper
public interface FileAttachDao {
	
	@Insert("""
			insert into fileAttach
				set regDate = now()
					, updateDate = now()
					, workLogId = #{workLogId}
					, fileName = #{fileName}
					, filePath = #{filePath}
					, fileSize = #{fileSize}
			""")
	void fileInsert(FileAttach fileAttach);

	

}
