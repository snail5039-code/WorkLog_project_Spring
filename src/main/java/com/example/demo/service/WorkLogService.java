package com.example.demo.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.WorkLogDao;
import com.example.demo.dto.FileAttach;
import com.example.demo.dto.WorkLog;

@Service
public class WorkLogService {
	
	private WorkLogDao workLogDao;
	private FileAttachService fileAttachService;
	private WorkChatAIService workChatAIService;
	private DocumentGeneratorService documentGeneratorService;
	// 의존성 주입
	public WorkLogService(WorkLogDao workLogDao, FileAttachService fileAttachService, WorkChatAIService workChatAIService, DocumentGeneratorService documentGeneratorService) {
		this.workLogDao = workLogDao;
		this.fileAttachService = fileAttachService;
		this.workChatAIService = workChatAIService;
		this.documentGeneratorService = documentGeneratorService;
	}
	
	public void writeWorkLog(WorkLog workLogData, int memberId) {
		this.workLogDao.writeWorkLog(workLogData, memberId);
	}

	public List<WorkLog> showList() {
		return this.workLogDao.showList();
	}

	public WorkLog showDetail(int id) {
		WorkLog workLog = this.workLogDao.showDetail(id); 
	    
	    if (workLog != null) {
	        List<FileAttach> fileAttaches = fileAttachService.getFilesByWorkLogId(id);
	        workLog.setFileAttaches(fileAttaches);  // 그래서 요거 worklog에 만들어줬음 리스트로 받을 수 있게!
	    }
	    
	    return workLog;
	}

	public int doModify(int id, WorkLog modifyData) {
		return this.workLogDao.doModify(id, modifyData);
	}

	public int getLastInsertId() {
		return this.workLogDao.getLastInsertId();
	}

	@Transactional // 전체 로직이 하나의 트랜잭션으로 묶이도록 처리
	public byte[] processWorkLog(WorkLog workLog, List<MultipartFile> files, int memberId) throws Exception {
		
        // 1. DTO에 작성자 ID 설정
        workLog.setMemberId(memberId);
        
        // 2. AI 분석 및 DOCX 데이터 생성 (WorkChatAIService에 위임)
        // 템플릿 파일이 files 리스트의 첫 번째 파일이라고 가정합니다.
        MultipartFile templateFile = files != null && !files.isEmpty() ? files.get(0) : null;
        // 템플릿 파일 준비
        if (templateFile == null || templateFile.isEmpty()) {
            throw new IllegalArgumentException("Docx 템플릿 파일이 누락되었습니다.");
        }
        
        // AI 보고서 생성 및 Docx 데이터 Map 반환 (DB 저장 로직 제거)
        Map<String, Object> docxData = workChatAIService.generateAndMapDocxData(workLog, templateFile);
    	// 3. ⭐️ Word 문서 생성 (여기서 docxBytes를 얻습니다) ⭐️
        byte[] docxBytes = documentGeneratorService.generateDocxReport(templateFile, docxData);
        
        // 3. DB에 업무 일지 저장
        // workLog DTO에는 이제 memberId와 AI가 채워준 summaryContent가 포함되어 있습니다.
        this.workLogDao.writeWorkLog(workLog, memberId);

        // 4. 저장된 WorkLog의 ID를 가져옴 (파일 첨부를 위해)
        int workLogId = this.workLogDao.getLastInsertId();
        
        // 5. 첨부 파일 처리
        if (workLogId > 0 && files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    this.fileAttachService.fileInsert(workLogId, file);
                }
            }
        }
        
        // 6. 생성된 Docx 파일의 byte 배열 반환 (컨트롤러로 전달)
        return docxBytes;
	}
	// ai가 내용 분석 후 docx 파일 바이너리 생성, 그리고 내용 채우고 생성 id 가져와옴 그리고 컨트롤러로 넘겨서 다운로드 완료 
	// 겁나 쉽게 그냥 요약한거 통째로 가져와서 넘기는 것
}
