package com.milk.batch.bean;

import java.math.BigDecimal;

public class GoodsHobBean {
	public long MergeSEQ;

	public int MergeCD;

	public String MergeType = "";

	public String GroupNM = "";

	public String MergeNM = "";

	public BigDecimal WeekQty1 = new BigDecimal("0");

	public BigDecimal WeekQty2 = new BigDecimal("0");

	public BigDecimal WeekQty3 = new BigDecimal("0");

	public BigDecimal WeekQty4 = new BigDecimal("0");

	public BigDecimal WeekQty5 = new BigDecimal("0");

	public BigDecimal WeekQty6 = new BigDecimal("0");

	public BigDecimal WeekQty7 = new BigDecimal("0");

	public int DefaultQty;

	public int WeekQty;

	public String Cause = "";

	public BigDecimal HobSum = new BigDecimal("0");
}
