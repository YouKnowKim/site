package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class UserPointBean extends UserBean {
	public long UserCD;

	public int PointSEQ;

	public Timestamp PointDT;

	public Timestamp ExpireDT;

	public BigDecimal HomePoint = new BigDecimal("0");

	public BigDecimal HomeEventPoint = new BigDecimal("0");

	public BigDecimal ShopPoint = new BigDecimal("0");

	public BigDecimal ShopEventPoint = new BigDecimal("0");

	public int PointType;

	public String PointRemark = "";

	public long OrderCD;
}
