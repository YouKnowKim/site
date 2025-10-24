package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class MilkbangFileDao {

	private String startDate;
	private String endDate;
	private String selectedAgency;
	
	private String stdYear;
	private String stdMonth;
	private String stdWeek;
	private String teamPersonCd;
	
	private String fileNm;
	private String fileUrl;
	private String downloadDt;
	private String uploadDt;
	private String uploadYn;
	private String fileStatus;
	private String insertDt;
	private String fileStatusNm;
	private String uploadYnNm;
	private String agencyCd;
	private String agencyNm;
	private String agencyType;
	private String teamPersonNm;
	private String deleteYn;
	private Integer no;
	private String sendYn;
}
