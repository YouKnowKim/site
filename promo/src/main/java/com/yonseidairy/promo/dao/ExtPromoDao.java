package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class ExtPromoDao {
	
	private String stdDate;
	private String promoTeamCd;
	private String promoPersonNm;
	private String agencyNm;
	
	private String deptNme;
	private String week1;
	private String week2;
	private String week3;
	private String week4;
	private String week5;
	private String sumWeek;
	private String avgWeek;
	private String beforeAvgWeek;
	private String fluctWeek;
	
	private String startDate;
	private String endDate;

}
