package com.milk.batch.bean;

import java.sql.Timestamp;
import java.util.ArrayList;

public class AgencyMilkbangBean extends CommonBean {
	public long AgencyCD;

	public String AgencyNM = "";

	public String MilkbangFileNM = "";

	public int MilkbangType;

	public Timestamp UploadDT;

	public int UploadYN;

	ArrayList<com.milk.batch.bean.AgencyMilkbangBean> arrAgencyMilkbang = new ArrayList<>();
}
