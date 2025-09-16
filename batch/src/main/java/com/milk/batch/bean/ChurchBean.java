package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class ChurchBean extends UserBean {
	public long ChurchCD;

	public String ChurchNM = "";

	public BigDecimal ChurchMileage = new BigDecimal("0");

	public String ChurchAddress1 = "";

	public String ChurchAddress2 = "";

	public String ChurchZipCD = "";

	public String ChurchTel = "";

	public long RequestUserCD;

	public String RequestUserNM = "";

	public String RequestUserRole = "";

	public String RequestCellPhone = "";

	public String RequestEmail = "";

	public String RequestContent = "";

	public int RequestStatus;

	public Timestamp AcceptDT;

	public long ChurchCD_Origin;
}
