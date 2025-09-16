package com.milk.batch.bean;

import java.math.BigDecimal;

public class GiftBean extends CommonBean {
	public long GiftCD;

	public String GiftNM = "";

	public String GiftImg = "";

	public String GiftContents;

	public BigDecimal SharePrice = new BigDecimal("0");

	public BigDecimal ShowPrice = new BigDecimal("0");

	public int ShowYN;

	public int DeleteYN;
}