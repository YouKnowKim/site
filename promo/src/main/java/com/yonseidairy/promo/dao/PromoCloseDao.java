package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class PromoCloseDao {

	/**
	 * 담당자 코드
	 */
	private String teamPersonCd;

	/**
	 * 담당자명
	 */
	private String teamPersonNm;

	/**
	 * 전체 건수
	 */
	private String totalCnt;

	/**
	 * 마감 건수
	 */
	private String closeCnt;

	/**
	 * 미저장 건수
	 */
	private String unSavedCnt;

	/**
	 * 마감일자 (최근 마감일)
	 */
	private String masterCloseDt;

	/**
	 * 마감 상태명 - 미저장: 마감건수 0, 미저장건수 > 0 - 마감전: 마감건수 0, 미저장건수 = 0 - 마감완료: 전체 마감, 미저장건수
	 * = 0 - 마감후 추가건: 일부 마감, 마감 후 새 건 추가됨 - 오류: 기타 상태
	 */
	private String masterCloseNm;

	/**
	 * 마감 비고
	 */
	private String masterCloseRemark;
	
	private String subStartDate;
	private String subEndDate;
	
	private String startDate;
	private String endDate;
	private String teamCd;

}
