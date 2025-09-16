package com.milk.batch.bean;

import java.math.BigDecimal;
import java.util.ArrayList;

public class AgencyBean extends CommonBean {

	public long AgencyCD;

	public String AgencyNM = "";

	public int AgencyType;

	public String AgencyTypeNM = "";

	public long TeamPersonCD;

	public String TeamPersonNM = "";

	public long HC_TeamPersonCD;

	public long HC_TeamPersonID;

	public long HC_TeamPersonNM;

	public String Area = "";

	public int AreaCD;

	public int AreaSubCD;

	public String ManagerNM = "";

	public String AgencyTel = "";

	public String AgencyCellPhone = "";

	public String AgencyAddress = "";

	public int AgencyCNT;

	public int ThisWeekCNT;

	public int LastWeekCNT;

	public String AgencyNO = "";

	public String BankUserNM = "";

	public String BankNM = "";

	public String BankNO = "";

	public BigDecimal SupplyPrice = new BigDecimal("0");

	public BigDecimal TaxPrice = new BigDecimal("0");

	public BigDecimal TotalPrice = new BigDecimal("0");

	public String VirtualBankNM = "";

	public String VirtualBankNO_home = "";

	public String VirtualBankNO_special = "";

	public String VirtualBankNO_soy = "";

	public long PromoTeamCD;

	public String PromoTeamNM = "";

	public int ArrangePersonCNT;

	public ArrayList<com.milk.batch.bean.AgencyBean> child = new ArrayList<>();

	public OrderGoodsBean clsMilkbang;

	public ArrayList<OrderGoodsBean> arrMilkbang = new ArrayList<>();
}
