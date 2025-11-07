package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class GoodsDao {
	
	private String deleteYn;
	private String no;
	private String misNm;
	private String salesMan;
	private String empyNme;
	private String matcode;
	private String changeType;
	private String teamPersonCd;
	
	/**
	 * 상품옵션코드 (Primary Key)
	 */
	private String goodsOptionCd;

	/**
	 * 상품코드
	 */
	private String goodsCd;

	/**
	 * 옵션명
	 */
	private String optionNm;

	/**
	 * 원가
	 */
	private String originPrice;

	/**
	 * 단가 (판매가)
	 */
	private String unitPrice;

	/**
	 * MIS 코드
	 */
	private String misCd;

	/**
	 * 월요일 배송 여부 (1: 배송, 0: 미배송)
	 */
	private String day1;

	/**
	 * 화요일 배송 여부 (1: 배송, 0: 미배송)
	 */
	private String day2;

	/**
	 * 수요일 배송 여부 (1: 배송, 0: 미배송)
	 */
	private String day3;

	/**
	 * 목요일 배송 여부 (1: 배송, 0: 미배송)
	 */
	private String day4;

	/**
	 * 금요일 배송 여부 (1: 배송, 0: 미배송)
	 */
	private String day5;

	/**
	 * 토요일 배송 여부 (1: 배송, 0: 미배송)
	 */
	private String day6;
}
