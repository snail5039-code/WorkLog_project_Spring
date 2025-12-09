package com.example.demo.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.demo.dto.Member;

@Service
public class HandoverTemplateService {

	public static final String FROM_NAME = "${from_name}";
	public static final String FROM_JOB = "${from_job}";
	public static final String FROM_SIGN = "${from_sign}";
	public static final String TO_NAME = "${to_name}";
	public static final String TO_JOB = "${to_job}";
	public static final String TO_SIGN = "${to_sign}";
	public static final String TITLE = "${title}";
	public static final String CONTENT = "${handover_content}";
	public static final String DATE = "${handover_date}";

	public Map<String, String> buildBaseValues(Member member, String toName, String toJob, String title, String content,
			String date, String fromJob) {

		Map<String, String> values = new HashMap<>();

		values.put(FROM_NAME, member.getName());
		values.put(FROM_JOB, fromJob);
		values.put(FROM_SIGN, member.getName());

		// 인수자
		values.put(TO_NAME, toName != null ? toName : "");
		values.put(TO_JOB, toJob != null ? toJob : "");
		values.put(TO_SIGN, (toName != null && !toName.isBlank()) ? toName + "(인)" : "");

		// 나머지
		values.put(TITLE, title != null ? title : "");
		values.put(CONTENT, content != null ? content : "");
		values.put(DATE, date != null ? date : "");

		return values;
	}
}
