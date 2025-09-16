package com.milk.batch.bean;

import java.sql.Timestamp;

public class CommonBean {
	public int SearchResultCNT;

	public int Search_Page;

	public int Search_TotalPage;

	public int Search_ShowCNT;

	public int Search_TotalCNT;

	public String Search_OrderCol = "";

	public String Search_OrderBy = "";

	public String SearchDT_Type = "";

	public String SearchFromDT = "";

	public String SearchToDT = "";

	public String Search_Limit = "";

	public Timestamp InsertDT = null;

	public long InsertUserCD;

	public String InsertUserNM = "";

	public String InsertUserID = "";

	public String InsertUserIP = "";

	public Timestamp UpdateDT = null;

	public long UpdateUserCD;

	public String UpdateUserNM = "";

	public String UpdateUserID = "";

	public String UpdateUserIP = "";

	public Timestamp DeleteDT = null;

	public long DeleteUserCD;

	public String DeleteUserNM = "";

	public String DeleteUserID = "";

	public String DeleteUserIP = "";

	public String DeleteRemark = "";

	public int DeleteYN;
}
