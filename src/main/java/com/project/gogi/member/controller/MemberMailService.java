package com.project.gogi.member.controller;

import java.io.UnsupportedEncodingException;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public interface MemberMailService {
	MimeMessage createMessage(String to) throws MessagingException, UnsupportedEncodingException;

	// 랜덤 인증 코드 전송
	String createKey();

	// 메일 발송
	String sendSimpleMessage(String to) throws Exception;
}