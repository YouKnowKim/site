package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MileageBean extends UserBean {
	public long UserCD;

	public String UserNM = "";

	public int MileageType;

	public int MileageSEQ;

	public Timestamp MileageDT;

	public Timestamp ExpireDT;

	public BigDecimal MileagePoint = new BigDecimal("0");

	public String MileageRemark = "";

	public long OrderCD;

	public long ChurchCD;
}
