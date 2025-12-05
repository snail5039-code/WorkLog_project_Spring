package com.example.demo.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.dao.WorkLogDao;
import com.example.demo.dto.FileAttach;
import com.example.demo.dto.WorkLog;

@Service
public class WorkLogService {
	
	@Value("${file.upload-dir}")
	private String uploadDir;
	
	private static class ResourceMultipartFile implements MultipartFile {
	    private final String name;
	    private final String originalFilename;
	    private final String contentType;
	    private final byte[] content;
	    
	    // 도큐먼트 서비스한테 파일 모양을 같게 하려고 하는 것임
	    public ResourceMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
	        this.name = name;
	        this.originalFilename = originalFilename;
	        this.contentType = contentType;
	        this.content = content;
	    }

	    @Override
	    public String getName() {
	        return name;
	    }

	    @Override
	    public String getOriginalFilename() {
	        return originalFilename;
	    }

	    @Override
	    public String getContentType() {
	        return contentType;
	    }

	    @Override
	    public boolean isEmpty() {
	        return (content == null || content.length == 0);
	    }

	    @Override
	    public long getSize() {
	        return content.length;
	    }

	    @Override
	    public byte[] getBytes() throws IOException {
	        return content;
	    }

	    @Override
	    public InputStream getInputStream() throws IOException {
	        return new ByteArrayInputStream(content);
	    }

	    @Override
	    public void transferTo(File dest) throws IOException, IllegalStateException {
	        throw new UnsupportedOperationException("File transfer is not supported for this resource.");
	    }
	}
	
	
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
	// 템플릿 파일명 결정 
	private String getTemplateFileName(String documentType) {
		if(documentType == null) {
			return "업무일지양식1.docx";
		}
		switch (documentType) {
		case "1":	
			return "업무일지양식3.docx";
		case "2":	
			return "업무일지양식4.docx";
		case "3":	
			return "업무일지양식5.docx";
		case "4":	
			return "업무일지양식6.docx";
		case "5":	
			return "업무일지양식7.docx";
		case "6" :
		default:
            return "업무일지양식1.docx";
		}
	}

	@Transactional
	public int processWorkLog(WorkLog workLog, List<MultipartFile> files, int memberId) throws Exception {

	    // 1) 먼저 DB에 workLog를 저장하여 workLogId 확보
	    this.workLogDao.writeWorkLog(workLog, memberId);
	    int workLogId = this.workLogDao.getLastInsertId();

	    // 2) 템플릿 파일 처리
	    MultipartFile templateFile = null;

	    if (files != null && !files.isEmpty() && !files.get(0).isEmpty()) {
	        templateFile = files.get(0); // 첫 파일이 템플릿
	    }

	    if (templateFile == null) {
	        String templateFileName = getTemplateFileName(workLog.getDocumentType());

	        ClassPathResource resource = new ClassPathResource("templates/" + templateFileName);
	        byte[] templateBytes = resource.getInputStream().readAllBytes();

	        templateFile = new ResourceMultipartFile(
	                templateFileName,
	                templateFileName,
	                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
	                templateBytes
	        );
	    }

	    // 3) AI로 내용 생성 + 매핑
	    Map<String, Object> docxData = workChatAIService.generateAndMapDocxData(workLog, templateFile);

	    // 제목이 있으면 반영
	    if (docxData.containsKey("title")) {
	        String generatedTitle = (String) docxData.get("title");
	        if (generatedTitle != null && !generatedTitle.trim().isEmpty()) {
	            workLog.setTitle(generatedTitle);
	        }
	    }

	    // 메인/요약도 반영
	    if (docxData.containsKey("mainContent")) {
	        workLog.setMainContent((String) docxData.get("mainContent"));
	    }
	    if (docxData.containsKey("summaryContent")) {
	        workLog.setSummaryContent((String) docxData.get("summaryContent"));
	    }

	    // 4) DOCX 생성 + 저장
	    String savedPath = documentGeneratorService.generateAndSaveDocx(templateFile, docxData, workLogId);

	    // 5) DB에 docxPath 업데이트 그래야 나중에도 다운로드 할 수 있음
	    workLogDao.updateDocxPath(workLogId, savedPath);

	    // 6) 첨부 파일 처리
	    if (files != null && files.size() > 1) {
	        for (int i = 1; i < files.size(); i++) {
	            MultipartFile file = files.get(i);
	            if (!file.isEmpty()) {
	                fileAttachService.fileInsert(workLogId, file);
	            }
	        }
	    }

	    return workLogId;
	}

}
