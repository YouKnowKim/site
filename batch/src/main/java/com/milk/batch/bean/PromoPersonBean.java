package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class PromoPersonBean extends CommonBean {
	public Timestamp PromoDT;

	public int PromoSEQ;

	public String PromoPersonNM = "";

	public long PromoTeamCD;

	public String PromoTeamNM = "";

	public int PromoType;

	public long TeamPersonCD;

	public String TeamPersonNM = "";

	public int TeamPersonCNT;

	public long TeamCD;

	public String TeamNM = "";

	public long AgencyCD;

	public String AgencyNM = "";

	public String CellPhoneNO = "";

	public String PromoWeek = "";

	public int PromoCloseYN;

	public int PromoCloseYN_Confirm;

	public int TeamPersonCloseYN;

	public int TeamPersonCloseYN_Confirm;

	public String StartDT = "";

	public String EndDT = "";

	public String ChangeReason = "";

	public int ChangeType;

	public long OriginAgencyCD;

	public BigDecimal ActualHobSum = new BigDecimal("0");
}
