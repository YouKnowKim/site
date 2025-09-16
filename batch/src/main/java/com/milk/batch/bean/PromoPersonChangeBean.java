package com.milk.batch.bean;

import java.sql.Timestamp;

public class PromoPersonChangeBean extends CommonBean {
	public Timestamp PromoDT;

	public long PromoSEQ;

	public long PromoTeamCD;

	public String PromoPersonNM = "";

	public int ChangeType;

	public String ChangeReason = "";

	public String PromoWeek = "";

	public int PromoType;

	public String PromoTeamNM = "";

	public int ThisWeekCNT;

	public int LastWeekCNT;
}
