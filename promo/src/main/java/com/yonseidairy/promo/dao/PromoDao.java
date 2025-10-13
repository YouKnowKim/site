package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class PromoDao {
	
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

}
