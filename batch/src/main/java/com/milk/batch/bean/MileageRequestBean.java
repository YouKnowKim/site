package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MileageRequestBean extends MileageGoodsBean {
	public long RequestCD;

	public Timestamp RequestDT;

	public int RequestType;

	public long RequestChurchCD;

	public String RequestChurchNM;

	public String RequestChurchAddress1;

	public String RequestChurchAddress2;

	public String RequestChurchZipCD;

	public String RequestChurchTel;

	public long RequestUserCD;

	public String RequestUserNM = "";

	public String RequestHomePhone = "";

	public String RequestCellPhone = "";

	public String RequestEmail = "";

	public String RequestZipCD = "";

	public String RequestAddress1 = "";

	public String RequestAddress2 = "";

	public String RequestRemark = "";

	public int RequestStatus;

	public String RequestStatusNM = "";

	public BigDecimal SalesMileage = new BigDecimal("0");

	public long MileageGoodsCD;

	public String MileageGoodsNM;

	public int DeleteYN;
}
