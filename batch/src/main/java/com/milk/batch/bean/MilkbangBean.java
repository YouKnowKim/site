package com.milk.batch.bean;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;

public class MilkbangBean extends OrderGoodsBean {
	public String FileNM = "";

	public String FileURL = "";

	public Timestamp DownloadDT;

	public long AgencyCD;

	public String AgencyNM = "";

	public int UploadYN;

	public Timestamp UploadDT;

	public int FileStatus;

	public String FileStatusNM = "";

	public Timestamp StartDT;

	public Timestamp EndDT;

	public int MergeCD;

	public String ComputeHobCause = "";

	public int TotalCNT;

	public int SaveCNT;

	public int NoSaveCNT;

	public int MasterCloseCNT;

	public ArrayList<com.milk.batch.bean.MilkbangBean> arrMilkbangGoods = new ArrayList<>();

	public ArrayList<com.milk.batch.bean.MilkbangBean> child = new ArrayList<>();
	
	public BigDecimal Day1 = new BigDecimal("0");
	
	public BigDecimal Day2 = new BigDecimal("0");
	
	public BigDecimal Day3 = new BigDecimal("0");
	
	public BigDecimal Day4 = new BigDecimal("0");
	
	public BigDecimal Day5 = new BigDecimal("0");
	
	public BigDecimal Day6 = new BigDecimal("0");
}
