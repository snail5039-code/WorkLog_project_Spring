package com.example.demo.util;


import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import kr.dogfoot.hwplib.object.HWPFile;
import kr.dogfoot.hwplib.reader.HWPReader;
import kr.dogfoot.hwplib.tool.textextractor.TextExtractMethod;
import kr.dogfoot.hwplib.tool.textextractor.TextExtractor;
 
@Component //파일텍스트 추출기(파일 형식 별로) 
public class FileTextExtractor {
	public String extractText(MultipartFile file) throws IOException {
		String filename = file.getOriginalFilename();
		if(filename == null) return "";
		// 파일 이름을 소문자로 변환
		String lowerFilename = filename.toLowerCase();
		if (lowerFilename.endsWith(".rtf")) {
            throw new IllegalArgumentException("지원하지 않는 문서 형식 (RTF)입니다. DOCX, DOC, PDF, HWP 파일을 사용해 주세요.");
       }
		// pdf 라이브러리 사용 파일 내용 읽어오기, 리턴으로 파일에서 오직 텍스트만 추출해서 하나의 긴 문자열로 반환, 밑에도 동일
		if(lowerFilename.endsWith(".pdf")) {
			try(PDDocument document = PDDocument.load(file.getInputStream())) {
				return new PDFTextStripper().getText(document);
			} //docx, doc은 객체를 새로 만들어야함
		} else if(lowerFilename.endsWith(".docx")) {
			try (XWPFDocument docuument = new XWPFDocument(file.getInputStream())) {
				return new XWPFWordExtractor(docuument).getText();
			}
		} else if(lowerFilename.endsWith(".doc")) {
			try (HWPFDocument docuument = new HWPFDocument(file.getInputStream())) {
				return new WordExtractor(docuument).getText();
			}
		} else if(lowerFilename.endsWith(".txt")) {
			return new String(file.getBytes(), StandardCharsets.UTF_8); // 바이트로 파일을 통으로 가져와 인코딩 시키는 것을 스트링 안에 집어넣는 것!
		} else if(lowerFilename.endsWith(".hwp")) { 
			try {
				HWPFile hwpFile = HWPReader.fromInputStream(file.getInputStream());
				return TextExtractor.extract(hwpFile, TextExtractMethod.AppendControlTextAfterParagraphText);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				throw new IOException("HWP 파일 텍스트 추출 중 오류 발생", e);
			}
		} else {
			throw new IllegalArgumentException("지원하지 않는 파일 형식입니다.");
		}
		return lowerFilename;
	}
	
//  hwp라이브러리 테스트 hwpx는 안되서 그냥 쓰자 	
//	public String test() {
//		System.out.println("asdasd");
//		HWPFile hwpFile;
//		String hwpText = null;
//		try {
//			hwpFile = HWPReader.fromFile("C:\\Users\\admin\\Downloads" + "\\나를 소개 합니다.hwp");
//			hwpText = TextExtractor.extract(hwpFile, TextExtractMethod.AppendControlTextAfterParagraphText);
//			System.out.println(hwpText);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		return hwpText;
//	}
}