package com.kakao.jPanda.kts.domain;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class ChatPartner {
	
	@NotEmpty
	private String memberId;
	
	@NotEmpty
	private String name;
	
	@NotEmpty
	private int unreadCount;
}