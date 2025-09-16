package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class TeamPersonBean extends CommonBean {
	public long TeamPersonCD;

	public String TeamPersonNM = "";

	public String LoginID = "";

	public String LoginPW = "";

	public int TeamPersonType;

	public String TeamPersonTypeNM = "";

	public int ManagerYN;

	public long TeamCD;

	public String TeamNM = "";

	public int AgencyYN;

	public long AgencyCD;

	public String AgencyNM = "";

	public int LoginYN;

	public String LoginIP = "";

	public String LoginBrowser = "";

	public String LoginSessionID = "";

	public Timestamp LastConnectDT;

	public int AgencyCNT;

	public BigDecimal ActualHobSum = new BigDecimal("0");

	public int PromoPersonCNT;

	public BigDecimal PromoHobAVG = new BigDecimal("0");

	public BigDecimal PrevActualHobSum = new BigDecimal("0");

	public int PrevPromoPersonCNT;

	public BigDecimal PrevPromoHobAVG = new BigDecimal("0");

	public BigDecimal ActualHobGab = new BigDecimal("0");

	public int PromoPersonGab;

	public long PromoTeamCD;
}
