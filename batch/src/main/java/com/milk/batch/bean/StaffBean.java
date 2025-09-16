package com.milk.batch.bean;

import java.sql.Timestamp;

public class StaffBean extends CommonBean {
	public int StaffCD;

	public int StoreCD;

	public int ManageStoreCD;

	public int ManageOutboundCompanyCD;

	public String StaffID = "";

	public String StaffNM = "";

	public String StaffNickNM = "";

	public String StaffPW = "";

	public boolean AdminYN;

	public boolean PurchaseAdminYN;

	public boolean RepayAdminYN;

	public int UserGroupCD;

	public String UserGroupNM = "";

	public int UserLevelCD;

	public int UserLevel;

	public int UserType;

	public String SerialNO1 = "";

	public String SerialNO2 = "";

	public boolean MerryYN;

	public Timestamp MerryDT;

	public String CellPhone1 = "";

	public String CellPhone2 = "";

	public String CellPhone3 = "";

	public String HomePhone1 = "";

	public String HomePhone2 = "";

	public String HomePhone3 = "";

	public String ZipCD1 = "";

	public String ZipCD2 = "";

	public String Address1 = "";

	public String Address2 = "";

	public String Email1 = "";

	public String Email2 = "";

	public String AdminRemark = "";

	public Timestamp LastConnectDT;

	public int LoginCNT;

	public boolean LoginYN;

	public Timestamp LoginDT;

	public String LoginIP = "";

	public String LoginBrowser = "";

	public String LoginSessionID = "";

	public int WorkType;

	public String StaffNO = "";

	public boolean LogoutYN = false;
}
