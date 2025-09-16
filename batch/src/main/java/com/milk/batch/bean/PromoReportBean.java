package com.milk.batch.bean;

import java.util.ArrayList;

public class PromoReportBean extends CommonBean {
	public long TeamPersonCD;

	public String TeamPersonNM;

	public long TeamCD;

	public String PromoTeamNM;

	public int ThisWeekCNT;

	public int LastWeekCNT;

	public int CompanyIN;

	public int VacationIN;

	public int NewIN;

	public int EtcIN;

	public int MoveIN;

	public int CompanyMove;

	public int VacationOut;

	public int CompanyOut;

	public int EtcOut;

	public int MoveOut;

	public String AgencyNM;

	public long PromoTeamCD;

	public int PromoType;

	public String PromoPersonNM;

	public ArrayList<PromoReportSubBean> arrSubList = new ArrayList<>();

	public int PromoRowCNT;

	public int AgencyTeamRowCNT;

	public int TeamRowCNT;

	public int MaxRowCNT;

	public int LastWeekAgencyCNT;

	public int ShowYN;

	public String LastWeekPromoPersonNM;

	public String LastWeekCellPhoneNO;

	public String ThisWeekPromoPersonNM;

	public String ThisWeekCellPhoneNO;

	public int AllocationThisCNT;

	public int AllocationLastCNT;

	public long AgencyCD;

	public String CellPhoneNO;

	public String PromoWeek;

	public String TeamNM;

	public String Area;
}
