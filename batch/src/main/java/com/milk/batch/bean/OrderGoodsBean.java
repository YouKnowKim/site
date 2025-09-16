package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

public class OrderGoodsBean extends OrderBean {
	public long OrderCD;

	public int OrderSEQ;

	public long GoodsCD;

	public long GoodsOptionCD;

	public String GoodsOptionNM = "";

	public int Quantity;

	public int ContractPeriod;

	public int WeekQty;

	public String WeekRemark = "";

	public BigDecimal UnitPrice = new BigDecimal("0");

	public long CouponCD;

	public String CouponNO = "";

	public BigDecimal CouponPrice = new BigDecimal("0");

	public BigDecimal OrderPrice = new BigDecimal("0");

	public BigDecimal PayPrice = new BigDecimal("0");

	public BigDecimal MakePrice = new BigDecimal("0");

	public BigDecimal FactoryPrice = new BigDecimal("0");

	public BigDecimal DeliveryFee = new BigDecimal("0");

	public BigDecimal TMFee = new BigDecimal("0");

	public BigDecimal DMFee = new BigDecimal("0");

	public int OrderStatus;

	public String OrderStatusNM = "";

	public int ProcessStatus;

	public String ProcessStatusNM = "";

	public Timestamp AgencyConfirmDT;

	public int CompleteStatus;

	public String CompleteStatusNM = "";

	public String DeliveryCompanyNM = "";

	public String DeliveryNO = "";

	public String OrderGoodsRemark = "";

	public int DeleteYN;

	public Timestamp PromoDT;

	public Timestamp PutDT;

	public Timestamp ExpireDT;

	public String GoodsOptionCD_Origin = "";

	public String GoodsOptionNM_Origin = "";

	public int OrderKindCD;

	public String OrderKind = "";

	public BigDecimal AgencyHob = new BigDecimal("0");

	public BigDecimal HQHob = new BigDecimal("0");

	public BigDecimal ActualHob = new BigDecimal("0");

	public String PromoGiftNM = "";

	public Timestamp GiveDT;

	public String GivePersonNM = "";

	public Timestamp StopDT;

	public String StopReason = "";

	public int SaveYN;

	public Timestamp SaveDT;

	public String SaveRemark = "";

	public int MasterCloseYN;

	public Timestamp MasterCloseDT;

	public String MasterCloseRemark = "";

	public int PromoType;

	public long PromoTeamCD;

	public String PromoTeamNM = "";

	public long PromoTeamCD_Before;

	public long TeamPersonCD;

	public String TeamPersonNM = "";

	public long TeamCD;

	public String TeamNM = "";

	public int ForceAddYN;

	public Timestamp HCDT;

	public long HCTeamPersonCD;

	public int HCStatus;

	public String HCStatus_s = "";

	public String HCContent = "";

	public int HCActionStatus;

	public String HCAction = "";

	public BigDecimal HCHob = new BigDecimal("0");

	public BigDecimal HCCheckHob = new BigDecimal("0");

	public BigDecimal TempHob = new BigDecimal("0");

	public int TM_SMS_MessageCD;

	public int TM_SMS_SendYN;

	public int TM_Mail_MessageCD;

	public int TM_Mail_SendYN;

	public int TM_OrderYN;

	public int TM_Tel_Status;

	public int TM_Tel_Result;

	public String TM_Tel_Remark = "";

	public Timestamp TM_Tel_DT;

	public String OpenMarketOrderCD = "";

	public String OpenMarketOrderSEQ = "";

	public String OpenMarketGoodsCD = "";

	public String OpenMarketOptionNM = "";

	public BigDecimal ContractHob = new BigDecimal("0");

	public BigDecimal ReContractHob = new BigDecimal("0");

	public BigDecimal NoContractHob = new BigDecimal("0");

	public int ArrangePersonCNT;

	public int InputPersonCNT;

	public int AgencyCNT;

	public int PromoPersonCNT;

	public int MilkMethod;

	public String UserID = "";

	public String UserNM = "";

	public int ServicePeriod;

	public Timestamp HopeDT;

	public int AlumnusYN = -1;

	public int StaffYN = -1;

	public int SMSYN = -1;

	public int MailYN = -1;

	public ArrayList<com.milk.batch.bean.OrderGoodsBean> child = new ArrayList<>();

	public int DuplicateYN = -1;

	public long DuplOrderCD;

	public int OneclickYN = -1;

	public long OneclickOrderCD;
}
