package com.example.demo.dao;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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

	
	
	@Select("""
			select *
				from fileAttach
				where workLogId = #{workLogId}
				order by desc
			""")
	List<FileAttach> getFilesByWorkLogId(int workLogId);

}
