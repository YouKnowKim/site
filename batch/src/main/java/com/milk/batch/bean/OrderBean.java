package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

public class OrderBean extends GoodsOptionBean {
	public long OrderCD;

	public long OrderCD_Origin;

	public String OrderCD_s = "";

	public Timestamp OrderDT;

	public Timestamp OrderDT_Origin;

	public String OrderDT_s = "";

	public int OrderType;

	public int OrderType_Origin;

	public String OrderType_s = "";

	public String OrderTypeNM = "";

	public long OrderUserCD;

	public long OrderUserCD_Origin;

	public String OrderUserCD_s = "";

	public String OrderUserNM = "";

	public String OrderUserNM_Origin = "";

	public String OrderUserNM_s = "";

	public String OrderUserID = "";

	public String OrderHomePhone = "";

	public String OrderHomePhone_Origin = "";

	public String OrderHomePhone_s = "";

	public String OrderCellPhone = "";

	public String OrderCellPhone_Origin = "";

	public String OrderCellPhone_s = "";

	public String OrderEmail = "";

	public String OrderEmail_Origin = "";

	public String OrderEmail_s = "";

	public String OrderZipCD = "";

	public String OrderZipCD_Origin = "";

	public String OrderZipCD_s = "";

	public String OrderAddress1 = "";

	public String OrderAddress1_Origin = "";

	public String OrderAddress1_s = "";

	public String OrderAddress2 = "";

	public String OrderAddress2_Origin = "";

	public String OrderAddress2_s = "";

	public String OrderRemark = "";

	public String OrderRemark_Origin = "";

	public String OrderRemark_s = "";

	public int ReceiveType;

	public int ReceiveType_Origin;

	public String ReceiveType_s = "";

	public String ReceiveUserNM = "";

	public String ReceiveUserNM_Origin = "";

	public String ReceiveUserNM_s = "";

	public String ReceiveHomePhone = "";

	public String ReceiveHomePhone_Origin = "";

	public String ReceiveHomePhone_s = "";

	public String ReceiveCellPhone = "";

	public String ReceiveCellPhone_Origin = "";

	public String ReceiveCellPhone_s = "";

	public String ReceiveEmail = "";

	public String ReceiveEmail_Origin = "";

	public String ReceiveEmail_s = "";

	public String ReceiveZipCD = "";

	public String ReceiveZipCD_Origin = "";

	public String ReceiveZipCD_s = "";

	public String ReceiveAddress1 = "";

	public String ReceiveAddress1_Origin = "";

	public String ReceiveAddress1_s = "";

	public String ReceiveAddress2 = "";

	public String ReceiveAddress2_Origin = "";

	public String ReceiveAddress2_s = "";

	public String StaffRemark = "";

	public String StaffRemark_Origin = "";

	public String StaffRemark_s = "";

	public BigDecimal TotalOrderPrice = new BigDecimal("0");

	public BigDecimal TotalOrderPrice_Origin = new BigDecimal("0");

	public BigDecimal TotalCouponPrice = new BigDecimal("0");

	public BigDecimal TotalCouponPrice_Origin = new BigDecimal("0");

	public BigDecimal TotalPayPrice = new BigDecimal("0");

	public BigDecimal TotalPayPrice_Origin = new BigDecimal("0");

	public BigDecimal TotalUseHomePoint = new BigDecimal("0");

	public BigDecimal TotalUseHomeEventPoint = new BigDecimal("0");

	public BigDecimal TotalUseShopPoint = new BigDecimal("0");

	public BigDecimal TotalUseShopEventPoint = new BigDecimal("0");

	public BigDecimal TotalSaveHomePoint = new BigDecimal("0");

	public BigDecimal TotalSaveShopPoint = new BigDecimal("0");

	public int DeleteYN;

	public String MilkbangFileNM = "";

	public String MilkBangWeek = "";

	public long AgencyCD;

	public String AgencyNM = "";

	public long AgencyCD_DB;

	public String AgencyNM_DB = "";

	public String AgencyTel = "";

	public int AgencyDeliveryYN;

	public String PromoPersonNM = "";

	public String PromoPersonNM_Origin = "";

	public String PromoPersonNM_Before = "";

	public String PostArea = "";

	public String AddressType = "";

	public int ForceAddYN;

	public BigDecimal TotalActualHob = new BigDecimal("0");

	public BigDecimal ActualHob = new BigDecimal("0");

	public BigDecimal ContractHob = new BigDecimal("0");

	public BigDecimal ReContractHob = new BigDecimal("0");

	public BigDecimal NoContractHob = new BigDecimal("0");

	public int ArrangePersonCNT;

	public int InputPersonCNT;

	public BigDecimal TotalHCHob = new BigDecimal("0");

	public long GiftCD;

	public String GiftNM = "";

	public long MessageCD;

	public int PG_Type;

	public int PG_PayYN;

	public int PG_PayType;

	public Timestamp PG_PayDT;

	public String PG_TID = "";

	public String PG_CardNM = "";

	public String PG_CardAuthNO = "";

	public String PG_CardNO = "";

	public String PG_CardMonth = "";

	public String PG_CardInterest = "";

	public String PG_BankNM = "";

	public String PG_BankNO = "";

	public String PG_BankReceiver = "";

	public String PG_BankSender = "";

	public String PG_BankLimitDT = "";

	public String PG_CashReceiptNO = "";

	public String PG_CashReceiptType = "";

	public int PG_CancelYN;

	public Timestamp PG_CancelPayDT;

	public String PG_CancelCashReceiptNO = "";

	public int OrderStatus;

	public String OrderStatusNM = "";

	public int ProcessStatus;

	public String ProcessStatusNM = "";

	public String OrderCD_S = "";

	public String OrderType_S = "";

	public String ProcessStatus_S = "";

	public String OrderUserType_S = "";

	public String AgencyCD_s = "";

	public String TeamPersonCD_s = "";

	public String OrderDT_Start = "";

	public String OrderDT_End = "";

	public String PayDT_Start = "";

	public String PayDT_End = "";

	public String Recommender = "";

	public long TeamPersonCD;

	public String TeamPersonNM = "";

	public int ChurchYN;

	public long ChurchCD;

	public BigDecimal ChurchMileage = new BigDecimal("0");

	public BigDecimal HomePoint = new BigDecimal("0");

	public BigDecimal HomeEventPoint = new BigDecimal("0");

	public BigDecimal ShopPoint = new BigDecimal("0");

	public BigDecimal ShopEventPoint = new BigDecimal("0");

	public int TM_MessageType;

	public int TM_SMS_TemplateCD;

	public int TM_SMS_TemplateType;

	public int TM_Mail_TemplateCD;

	public int TM_Mail_TemplateType;

	public ArrayList<OrderGoodsBean> arrOrderGoods = new ArrayList<>();

	public int MergeCD;

	public ArrayList<com.milk.batch.bean.OrderBean> child = new ArrayList<>();

	public int OrderCNT;

	public int OrderMonth;
}
