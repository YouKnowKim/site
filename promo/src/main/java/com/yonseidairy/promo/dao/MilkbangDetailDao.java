package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class MilkbangDetailDao {
	
// ==================== input parameter ====================
	private String startDate;
	private String endDate;
	private String subStartDate;
	private String subEndDate;
	private String addCondition;

// ==================== TC_MILKBANG 기본 정보 ====================
	
	private String deptNme;
	private String no;
	private String promoTeamNm;
	private String issueStatus;
	
	private String hcSaveYn;
	
	private String agencyCdMis;
	
	private String status;
	
	private String orderKindCdNm;
	
	private String hcStatusNm;
	
	private String hcActionStatusNm;
	
	private String misCd;
	
	private String duplicateNm;

	/** 주문 코드 */
	private String orderCd;

	/** 주문 일자 */
	private String orderDt;

	/** 주문 유형 */
	private String orderType;

	/** 주문자명 */
	private String orderUserNm;

	/** 주문자 집전화 */
	private String orderHomePhone;

	/** 주문자 휴대폰 */
	private String orderCellPhone;

	/** 주문자 주소1 */
	private String orderAddress1;

	/** 수령인명 */
	private String receiveUserNm;

	/** 수령인 집전화 */
	private String receiveHomePhone;

	/** 수령인 휴대폰 */
	private String receiveCellPhone;

	/** 수령인 주소1 */
	private String receiveAddress1;

	/** 수령인 주소2 */
	private String receiveAddress2;

	/** 총 주문 금액 */
	private String totalOrderPrice;

	/** 총 결제 금액 */
	private String totalPayPrice;

	/** 삭제 여부 */
	private String deleteYn;

	/** 밀크방 파일명 */
	private String milkbangFileNm;

	/** 대리점 코드 */
	private String agencyCd;

	/** 대리점 전화번호 */
	private String agencyTel;

	/** 대리점 배송 여부 */
	private String agencyDeliveryYn;

	/** 판촉 담당자명 */
	private String promoPersonNm;

	/** 판촉 담당자명 원본 */
	private String promoPersonNmOrigin;

	/** 우편 지역 */
	private String postArea;

	/** 강제 추가 여부 (밀크방) */
	private String forceAddYn;

	/** 저장 여부 (밀크방) */
	private String saveYn;
	
	private String totStatus;

	/** PG 타입 */
	private String pgType;

	/** PG 결제 여부 */
	private String pgPayYn;

	/** PG 결제 유형 */
	private String pgPayType;

	/** PG 결제 일시 */
	private String pgPayDt;

	/** 등록 일시 (밀크방) */
	private String insertDt;

	/** 수정 일시 (밀크방) */
	private String updateDt;

	// ==================== TC_MILKBANG 중복 정보 ====================

	/** 중복 여부 */
	private String duplicateYn;

	/** 중복 주문 코드 */
	private String duplOrderCd;

	// ==================== TC_MILKBANGGOODS 상품 정보 ====================

	/** 주문 순번 */
	private String orderSeq;

	/** 상품 코드 */
	private String goodsCd;

	/** 상품 옵션 코드 */
	private String goodsOptionCd;

	/** 상품 옵션명 */
	private String goodsOptionNm;

	/** 수량 */
	private String quantity;

	/** 계약 기간 (개월) */
	private String contractPeriod;

	/** 주간 수량 (주당 배송 횟수) */
	private String weekQty;

	/** 주간 비고 */
	private String weekRemark;

	/** 단가 */
	private String unitPrice;

	/** 주문 금액 */
	private String orderPrice;

	/** 판촉 일자 */
	private String promoDt;

	/** 투입 일자 */
	private String putDt;

	/** 만료 일자 */
	private String expireDt;

	/** 상품 옵션 코드 원본 */
	private String goodsOptionCdOrigin;

	/** 상품 옵션명 원본 */
	private String goodsOptionNmOrigin;

	/** 주문 종류 코드 */
	private String orderKindCd;

	/** 주문 종류 */
	private String orderKind;

	/** 대리점 홉 */
	private String agencyHob;

	/** 본사 홉 */
	private String hqHob;

	/** 실제 홉 */
	private String actualHob;

	/** 판촉 선물명 */
	private String promoGiftNm;

	/** 지급 일자 */
	private String giveDt;

	/** 지급자명 */
	private String givePersonNm;

	/** 중지 일자 */
	private String stopDt;

	/** 중지 사유 */
	private String stopReason;

	/** 저장 여부 (상품) */
	private String goodsSaveYn;

	/** 저장 일시 (상품) */
	private String goodsSaveDt;

	/** 저장 비고 */
	private String saveRemark;

	/** 마스터 마감 여부 */
	private String masterCloseYn;

	/** 마스터 마감 일시 */
	private String masterCloseDt;

	/** 마스터 마감 비고 */
	private String masterCloseRemark;

	/** 판촉팀 코드 */
	private String promoTeamCd;

	/** 팀원 코드 */
	private String teamPersonCd;

	/** 팀 코드 */
	private String teamCd;

	/** 강제 추가 여부 (상품) */
	private String goodsForceAddYn;

	// ==================== TM 정보 ====================

	/** TM SMS 메시지 코드 */
	private String tmSmsMessageCd;

	/** TM SMS 발송 여부 */
	private String tmSmsSendYn;

	/** TM 메일 메시지 코드 */
	private String tmMailMessageCd;

	/** TM 메일 발송 여부 */
	private String tmMailSendYn;

	/** TM 주문 여부 */
	private String tmOrderYn;

	/** TM 전화 상태 */
	private String tmTelStatus;

	/** TM 전화 결과 */
	private String tmTelResult;

	/** TM 전화 비고 */
	private String tmTelRemark;

	/** TM 전화 일시 */
	private String tmTelDt;

	// ==================== 오픈마켓 정보 ====================

	/** 오픈마켓 주문 코드 */
	private String openMarketOrderCd;

	/** 오픈마켓 주문 순번 */
	private String openMarketOrderSeq;

	// ==================== 포인트 정보 ====================

	/** 적립 포인트 */
	private String savePoint;

	/** 직원 적립 포인트 */
	private String saveStaffPoint;

	/** 홈 적립 포인트 */
	private String saveHomePoint;

	/** 쇼핑 적립 포인트 */
	private String saveShopPoint;

	// ==================== 상품 용량 정보 ====================

	/** 용량 */
	private String capacity;

	// ==================== 해피콜 정보 ====================

	/** 해피콜 일자 */
	private String hcDt;

	/** 해피콜 담당자 코드 */
	private String hcTeamPersonCd;

	/** 해피콜 상태 */
	private String hcStatus;

	/** 해피콜 내용 */
	private String hcContent;

	/** 해피콜 조치 상태 */
	private String hcActionStatus;

	/** 해피콜 조치 내용 */
	private String hcAction;

	/** 해피콜 홉 */
	private String hcHob;

	/** 해피콜 체크 홉 */
	private String hcCheckHob;

	// ==================== 조인 테이블 정보 ====================

	/** 팀원명 (tc_teamperson) */
	private String teamPersonNm;

	/** 팀원 유형 (tc_teamperson) */
	private String teamPersonType;

	/** 관리자 여부 (tc_teamperson) */
	private String managerYn;

	/** 팀명 (tc_team) */
	private String teamNm;

	/** 대리점명 (tc_agency) */
	private String agencyNm;

}
