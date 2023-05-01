package com.kakao.jPanda.lhw.dao;

import java.util.List;

import com.kakao.jPanda.lhw.domain.ReviewDto;
import com.kakao.jPanda.lhw.domain.TalentDto;

public interface TalentDao {

	// 재능 판매 상세 페이지 불러오기
	TalentDto selectBoardTalentByTalentNo(Long talentNo);
	
	// 리뷰 리스트 불러오기
	List<ReviewDto> selectReivewListByTalentNo(Long talentNo);
	
	// 리뷰 인서트
	int insertReview(ReviewDto review);
	
	// 리뷰 업데이트
	int updateReview(ReviewDto review);
	
	// 리뷰 삭제
	int deleteReview(ReviewDto review);

	int updateTalent(Long talentNo);
	
	
	
}
