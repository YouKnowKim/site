package com.milk.batch.bean;

import java.math.BigDecimal;

public class AgencyGiftBean extends GiftBean {
	public long AgencyCD;

	public long GiftCD;

	public int Period;

	public BigDecimal AgencySharePrice = new BigDecimal("0");

	public BigDecimal AgencyShowPrice = new BigDecimal("0");

	public int DeleteYN;
}
