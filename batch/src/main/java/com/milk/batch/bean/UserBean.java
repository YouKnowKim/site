package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class UserBean extends CommonBean {
	public long UserCD;

	public String UserID = "";

	public String UserPW = "";

	public String UserPW_Origin = "";

	public String UserNM = "";

	public String Email = "";

	public String CellPhoneNO = "";

	public String HomePhoneNO = "";

	public Timestamp BirthDay;

	public String Gender = "";

	public String MarryYN = "";

	public String ZipCD = "";

	public String Address1 = "";

	public String Address2 = "";

	public String Address = "";

	public int EmailYN;

	public int SMSYN;

	public int JoinType;

	public Timestamp JoinDT;

	public BigDecimal HomePoint = new BigDecimal("0");

	public BigDecimal HomeEventPoint = new BigDecimal("0");

	public BigDecimal ShopPoint = new BigDecimal("0");

	public BigDecimal ShopEventPoint = new BigDecimal("0");

	public BigDecimal TotalPoint = new BigDecimal("0");

	public int StaffYN;

	public String StaffNO = "";

	public String StaffDepartment = "";

	public BigDecimal StaffJoinPoint = new BigDecimal("0");

	public int AlumnusYN;

	public String AlumnusYear = "";

	public String AlumnusDepartment = "";

	public BigDecimal AlumnusJoinPoint = new BigDecimal("0");

	public Timestamp LastConnectDT;

	public int LoginCNT;

	public int LoginYN;

	public String LoginIP = "";

	public String LoginBrowser = "";

	public String LoginSessionID = "";

	public String LatteID = "";

	public String CertifyID = "";

	public String CertifyWay = "";

	public Timestamp DeleteDT;

	public String DeleteRemark = "";

	public int DeleteYN;

	public long AgencyCD;

	public String AgencyNM = "";

	public String AgencyTel = "";

	public int DeliveryYN;

	public String DeleteDT_Start = "";

	public String DeleteDT_End = "";

	public String JoinDT_Start = "";

	public String JoinDT_End = "";

	public String LastConnectDT_Start = "";

	public String LastConnectDT_End = "";

	public int OrderMonth_Milk;

	public int OrderMonth_Shop;

	public int OrderMonth;

	public int OrderCNT_Milk;

	public int OrderCNT_Shop;

	public int OrderCNT;

	public int ShowType;

	public int ChurchYN;

	public long ChurchCD;

	public BigDecimal ChurchMileage = new BigDecimal("0");

	public String ChurchNM = "";

	public int ChurchMasterYN;
}
