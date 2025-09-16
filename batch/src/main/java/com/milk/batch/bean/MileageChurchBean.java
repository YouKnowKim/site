package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class MileageChurchBean extends UserBean {
	public long ChurchCD;

	public int MileageType;

	public int MileageSEQ;

	public Timestamp MileageDT;

	public Timestamp ExpireDT;

	public BigDecimal MileagePoint = new BigDecimal("0");

	public String MileageRemark = "";

	public long RequestCD;

	public BigDecimal ChurchMileage = new BigDecimal("0");
}
