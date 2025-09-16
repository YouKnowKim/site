package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class CouponBean extends GoodsOptionBean {
	public long CouponCD;

	public long CouponCD_Origin;

	public String CouponCD_s = "";

	public String CouponNM = "";

	public String CouponNM_Origin = "";

	public String CouponNM_s = "";

	public String CouponNO = "";

	public String CouponNO_Origin = "";

	public String CouponNO_s = "";

	public String CouponNO_enc = "";

	public String CouponNO_enc_Origin = "";

	public String CouponNO_enc_s = "";

	public String CouponNO_start = "";

	public String CouponNO_start_Origin = "";

	public String CouponNO_start_s = "";

	public String CouponNO_end = "";

	public String CouponNO_end_Origin = "";

	public String CouponNO_end_s = "";

	public int CouponType;

	public int CouponType_Origin;

	public String CouponType_s = "";

	public Timestamp StartDT = null;

	public Timestamp StartDT_Origin = null;

	public String StartDT_s = "";

	public Timestamp EndDT = null;

	public Timestamp EndDT_Origin = null;

	public String EndDT_s = "";

	public BigDecimal DCPrice = new BigDecimal("0");

	public BigDecimal DCPrice_Origin = new BigDecimal("0");

	public BigDecimal DCPercent = new BigDecimal("0");

	public BigDecimal DCPercent_Origin = new BigDecimal("0");

	public long GoodsOptionCD;

	public long GoodsOptionCD_Origin;

	public String GoodsOptionCD_s = "";

	public String GoodsOptionNM = "";

	public String GoodsOptionNM_Origin = "";

	public String GoodsOptionNM_s = "";

	public long AgencyCD;

	public long AgencyCD_Origin;

	public String AgencyCD_s = "";

	public String AgencyNM = "";

	public String AgencyNM_Origin = "";

	public String AgencyNM_s = "";

	public long UserCD;

	public String UserNM = "";

	public Timestamp DownloadDT = null;

	public int UseYN;

	public Timestamp UseDT = null;
}
