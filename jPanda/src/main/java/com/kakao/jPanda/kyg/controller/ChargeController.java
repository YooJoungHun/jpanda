package com.kakao.jPanda.kyg.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.kakao.jPanda.kyg.domain.ChargeDto;
import com.kakao.jPanda.kyg.domain.CouponUseDto;
import com.kakao.jPanda.kyg.domain.PaymentDto;
import com.kakao.jPanda.kyg.service.ChargeService;
import com.kakao.jPanda.kyg.service.Paging;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Controller
@RequestMapping("/charge")		
public class ChargeController {
	
	private final ChargeService chargeService;
	
	@Autowired
	public ChargeController(ChargeService chargeService) {
		this.chargeService = chargeService;
	}
	
	/*
	 * 메인페이지
	 * 결제수단, RATIO를 테이블에 List형식으로 나타냄
	 * 충전내역, chargeHistory를 테이블에 List형식으로 나타냄
	 * button에 따라 pageStatus의 값을 변경, pageStatus의 값에 따라 returnPage를 다르게 전달
	 * Model	TB payment -> method, bonusRatio 
	 * @param	HttpSession, ChargeDto chargeDto, String currentPage, String pageStatus, Model
	 * @return	kyg/chargePage, kyg/chargeHistoryPage
	 */
	@GetMapping(value = "")
	public String chargePage(HttpSession session, ChargeDto chargeDto, String currentPage, String pageStatus, Model model) {
		String returnPage = "kyg/chargePage";
		if(pageStatus == null) pageStatus = "0";
		log.info("ChargeContoller chargePage() pageStatus -> {}", pageStatus);
		
		if (session.getAttribute("memberId") == null) {
			return "redirect:/login";
		}
		
		String chargerId = (String) session.getAttribute("memberId");
		log.info("ChargeContoller chargePage() Start...");
		
		chargeDto.setChargerId(chargerId);
		log.info("ChargeContoller chargePage() chargerId -> {}", chargerId);
		
		// 페이징 작업
		log.info("ChargeContoller chargePage() chargeDto -> {}", chargeDto);
		log.info("ChargeContoller chargePage() currentPage -> {}", currentPage);
		
		int totalChargeCnt = chargeService.totalChargeCnt(chargerId);
		log.info("ChargeContoller chargePage() totalChargeCnt -> {}", totalChargeCnt);
		
		Paging page = new Paging(totalChargeCnt, currentPage);
		chargeDto.setStartRow(page.getStartRow());
		chargeDto.setEndRow(page.getEndRow());
		
		// 충전수단
		List<PaymentDto> getPaymentList = chargeService.findPaymentList();
		log.info("ChargeContoller chargePage() getPaymentList.size() -> {}", getPaymentList.size());
		
		// 충전내역
		List<ChargeDto> getBambooChargeList = chargeService.findBambooChargeList(chargeDto);
		log.info("ChargeContoller chargePage() getBambooChargeList.size() -> {}", getBambooChargeList.size());
		
		model.addAttribute("listPayment", getPaymentList);
		model.addAttribute("listChargeHistory", getBambooChargeList);
		model.addAttribute("pageStatus", pageStatus);
		model.addAttribute("totalChargeCnt", totalChargeCnt);
		model.addAttribute("page", page);
		
		if (pageStatus.equals("0")) {
			log.info("ChargeContoller chargePage() 충전하기 페이지 pageStatus(0) -> {}", pageStatus);
			returnPage = "kyg/chargePage";
		} else if (pageStatus.equals("1")) {
			log.info("ChargeContoller chargePage() 충전내역 페이지 pageStatus(1) -> {}", pageStatus);
			returnPage = "kyg/chargeHistoryPage";
		} else 	{
			log.info("ChargeContoller chargePage() default -> {}", pageStatus);
			returnPage = "kyg/chargePage";
		}
		 
		return returnPage;
	}
	
	/*
	 * 밤부 충전
	 * chargePage에서 ajax 요청 처리
	 * 전달된 data를 ChargeDto에 저장 후 DB에 삽입
	 * @param	ChargeDto
	 * @return	resultMap	/	resultMap을 return하여 callback시 success, fail에 따라 resultMap.put()을 console에 출력 
	 */
	@ResponseBody
	@PostMapping("/charge") 
	public Map<String, String> chargeAdd(@RequestBody ChargeDto chargeDto, HttpSession session) {
		String chargerId = (String) session.getAttribute("memberId");
		log.info("ChargeContoller charge() Start...");
		log.info("ChargeContoller checkAvailableCoupon() chargerId -> {}", chargerId);
		chargeDto.setChargerId(chargerId);
		log.info("ChargeContoller checkAvailableCoupon() couponUseDto.toString() -> {}", chargeDto.toString());
		
		int resultCharge = chargeService.addCharge(chargeDto);
		Map<String, String> resultMap = new HashMap<>();
		
		if(resultCharge > 0) {
			log.info("ChargeController charge() resultCharge 완료");
			 resultMap.put("result", "success");
			return resultMap;
			
		} else {
			log.error("ChargeContoller charge() resultCharge 실패");
			resultMap.put("result", "fail");
			return resultMap;
		}
		
	}
	
	/*
	 *	이용 가능한 쿠폰 체크
	 *	chargePage에서 ajax 요청 처리
	 *	전달받은 memberId와 couponCode를 TB coupon, TB coupon_use와 비교하여 validation check
	 *	사용 불가능한 쿠폰 : resultInt 0을 반환, 사용 가능한 쿠폰 : resultInt 1은 반환 ,couponValue를 반환
	 *	@param	CouponUseDto
	 *	@return	checkedcouponUseDto
	 */
	@GetMapping(value = "/check-available-coupon")
	@ResponseBody
	public CouponUseDto checkAvailableCoupon(HttpSession session, CouponUseDto couponUseDto) {
		String memberId = (String) session.getAttribute("memberId");
		CouponUseDto checkedcouponUseDto = new CouponUseDto();				
		log.info("ChargeContoller checkAvailableCoupon() Start...");
		log.info("ChargeContoller checkAvailableCoupon() memberId -> {}", memberId);
		couponUseDto.setMemberId(memberId);
		log.info("ChargeContoller checkAvailableCoupon() couponUseDto.toString() -> {}", couponUseDto.toString());
		
		// 사용 결과만 가져옴 -> 사용 가능한 쿠폰, 사용 했던 쿠폰을 비교하여, 회원이 사용했던 이력이 있는 쿠폰의 결과를 가져와 사용가능 여부를 따짐
		int resultInt = chargeService.checkAvailableCoupon(couponUseDto);
		
		// 충전 금액과 쿠폰의 금액을 차감해 실제 충전에 사용되는 금액을 구하기 위해 쿠폰의 금액을 가져옴
		Long couponValue = chargeService.getAvailAmountCoupon(couponUseDto);
		
		checkedcouponUseDto.setResult(resultInt);
		checkedcouponUseDto.setCouponValue(couponValue);
		
		log.info("ChargeContoller checkAvailableCoupon resultInt-> {}", resultInt);
		log.info("ChargeContoller checkAvailableCoupon couponValue-> {}", couponValue);
		
		return checkedcouponUseDto;
	}
	
	/*
	 * 총보유밤부
	 * chargePage에서 ajax 요청 처리
	 * memberId에 따른 총 보유 bamboo를 계산하여 반환
	 * @param	memberId
	 * @return	foundTotalBambooStr
	 */
	@GetMapping(path = "/members/total-bamboo")
	@ResponseBody
	public String  totalBambooByMemberId(HttpSession session) {
		String memberId = (String) session.getAttribute("memberId");
		log.info("ChargeContoller totalBambooByMemberId Start...");
		log.info("ChargeContoller totalBambooByMemberId() memberId -> {}", memberId);
		
		Long foundTotalBambooByMemberId = chargeService.findTotalBambooByMemberId(memberId);
		
		String  foundTotalBambooByMemberIdStr =  Long.toString(foundTotalBambooByMemberId);
		
		log.info("ChargeContoller totalBambooByMemberId calculatedTotalBamboo -> {}", foundTotalBambooByMemberIdStr);
		
		return foundTotalBambooByMemberIdStr;
	}
	
}
	


