package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class EdeliveryBean extends CommonBean {
	public long OrderCD;

	public long GoodsCD;

	public long GoodsOptionCD;

	public String GoodsNM = "";

	public int InsertCNT;

	public int Qty;

	public int Period;

	public BigDecimal GoodsPrice = new BigDecimal("0");

	public String Remark = "";

	public Timestamp OrderDT;

	public String GoodsImage = "";

	public String Capacity = "";
}