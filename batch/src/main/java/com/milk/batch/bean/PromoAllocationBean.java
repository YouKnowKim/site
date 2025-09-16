package com.milk.batch.bean;

import java.util.ArrayList;

public class PromoAllocationBean extends CommonBean {
	public boolean ErrorYN = false;

	public String ErrorReason = "";

	public ArrayList<PromoPersonBean> arrPromoPerson = new ArrayList<>();

	public long lngPromoTeamCD;

	public String strPromoTeamNM = "";

	public String strAllocationPeriod = "";

	public String strPromoDT = "";

	public String strPromoMonth = "";

	public String strPromoWeek = "";

	public String strThisWeekCNT = "";

	public String strLastWeekCNT = "";

	public String strChangeCNT = "";

	public String strMemo = "";

	public ArrayList<PromoPersonChangeBean> arrPromoPersonChange = new ArrayList<>();

	public ArrayList<PromoPersonChangeBean> arrPromoPersonIncrease = new ArrayList<>();

	public ArrayList<PromoPersonChangeBean> arrPromoPersonDecrease = new ArrayList<>();
}
