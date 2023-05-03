package com.kakao.jPanda.lhw.domain;

import java.sql.Timestamp;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BambooUseDto {

	private Long purchaseNo;
	private String buyerId;
	private Long talentNo;
	private Timestamp purchaseDate;
	private Long useBamboo;
	
	
	// 잔여 포인트 조회용
	private Long chargeBamboo;
}
