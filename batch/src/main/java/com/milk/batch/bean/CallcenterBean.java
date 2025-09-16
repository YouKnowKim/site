package com.milk.batch.bean;

import java.sql.Timestamp;

public class CallcenterBean extends CommonBean {
	public long ClaimCD;

	public long UserCD;

	public String UserNM = "";

	public String UserCellPhone = "";

	public String UserAddress = "";

	public int ClaimType;

	public String ClaimType_S = "";

	public String ClaimText = "";

	public Timestamp ClaimDT;

	public long ClaimStaffCD;

	public String ClaimStaffNM = "";

	public String ProcessText = "";

	public Timestamp ProcessDT;

	public long ProcessStaffCD;

	public String ProcessStaffNM = "";

	public int ProcessYN;

	public String ResultText = "";

	public Timestamp ResultDT;

	public long ResultStaffCD;

	public String ResultStaffNM = "";

	public long AgencyCD;

	public String AgencyNM = "";

	public String Remark = "";

	public long GoodsCD;

	public String GoodsNM = "";

	public long GoodsOptionCD;

	public String GoodsOptionNM = "";

	public int Capacity;

	public String ShelfLife = "";

	public String FactoryNM = "";

	public String BuyPlaceNM = "";

	public String ClaimDT_Start = "";

	public String ClaimDT_End = "";
}
