package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class MilkbangFileDao {

	private String startDate;
	private String endDate;
	private String selectedAgency;
	
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
	private Integer no;
}
