package com.milk.batch.bean;

import java.math.BigDecimal;

public class MileageGoodsBean extends ChurchBean {
	public long MileageGoodsCD;

	public long ChurchCD;

	public String MileageGoodsNM = "";

	public int ShowSeq;

	public String MileageGoodsImg = "";

	public int MileageGoodsType;

	public String MileageGoodsIntro = "";

	public BigDecimal GoodsMileage = new BigDecimal("0");

	public BigDecimal SalesMileage = new BigDecimal("0");

	public String BasicInfo = "";

	public String ContentHTML = "";

	public int SalesYN;

	public int DeleteYN;
}
