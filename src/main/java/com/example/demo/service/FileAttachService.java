package com.example.demo.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.FileAttachDao;
import com.example.demo.dto.FileAttach;

@Service
public class FileAttachService {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	private FileAttachDao fileAttachDao;
	// 의존성 주입
	public FileAttachService(FileAttachDao fileAttachDao) {
		this.fileAttachDao = fileAttachDao;
	}
	
	public void fileInsert(int workLogId, MultipartFile file) {
		
		String originalFileName = file.getOriginalFilename(); // 옆에 저이름은 원래 리액트에서 받아올때 이름임
		String addressFileName = UUID.randomUUID().toString() + "_" + originalFileName; //이거는 파일 찾을 주소 할당하고 이름하고 같이 표기 하는 느낌임
		
		// 업로드 디렉토리가 없으면 생성
		File uploadPath = new File(uploadDir);
		if(!uploadPath.exists()) {
			uploadPath.mkdir();
		}
		//혹시나 처음에 파일 저장 폴더가 없으면 생성하라는 의미
		
		File dest = new File(uploadDir, addressFileName);
		
		// 디스크에 파일 저장 
		try {
			file.transferTo(dest); //transferTo요거는 멀피파트파일의 기능 중하나 임시로 저장된것들 실제로 저장시키는 역할을 함 
			
			FileAttach fileAttach = new FileAttach();
			fileAttach.setWorkLogId(workLogId);
			fileAttach.setFileName(originalFileName);
			fileAttach.setFileSize(file.getSize());
			fileAttach.setFilePath(addressFileName);
			
			this.fileAttachDao.fileInsert(fileAttach);
		} catch (IOException e) {
			System.out.println("저장 실패 에러 발생");
		}
	}
	public List<FileAttach> getFilesByWorkLogId(int workLogId) {
        // WorkLog 상세 조회 시 첨부 파일 정보를 함께 보여주기 위해 필요합니다.
        return this.fileAttachDao.getFilesByWorkLogId(workLogId);
    }

	public String getOriginalFilename(String filePath) {
		return fileAttachDao.getOriginalFilename(filePath);
	}
}