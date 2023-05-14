package com.kakao.jPanda.board.domain;

import lombok.Data;

@Data
public class FiltersDto {
	
	private Long lowerCategoryNo;
	private Long upperCategoryNo;
	private String filter;
	private String search;
	
	// 페이징
	// 전체 글의 갯수
		private int totalCount;			
		// 전체 페이지의 수
		private int totalPage;
		// 페이지당 글의 수
		private int perPage = 12;
		// 전체 블럭의 수
		private int totalBlock;
		// 페이지당 블럭의 수
		private int pageBlock = 10;		
		// 현재 블럭 번호
		private int currentPage = 1;	
		// 페이지 시작번호
		private int startNum;			
		// 페이지 끝번호 
		private int lastNum;
		// 마지막 블럭 조사
		private int lastCheck;
		// 페이지 시작 행 
		private int startRow;
		// 페이지의 마지막 행
		private int endRow;
		
		public void setTotalCount(int totalCount) {
			this.totalCount = totalCount;
			
			startRow = (currentPage -1) * perPage + 1;
			endRow = startRow + perPage - 1;				
			totalPage = (int)Math.ceil((double)totalCount / perPage);
			startNum = currentPage - (currentPage - 1) % pageBlock;
			lastNum = startNum + pageBlock -1;	
			if(lastNum > totalPage) {
				lastNum = totalPage;
			}
		}
}
