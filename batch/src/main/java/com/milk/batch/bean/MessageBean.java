package com.milk.batch.bean;

import java.sql.Timestamp;

public class MessageBean extends CommonBean {
	public long MessageCD;

	public int MessageType;

	public long AgencyCD;

	public String AgencyNM = "";

	public String AgencyTel = "";

	public long UserCD;

	public String UserNM = "";

	public String CellPhoneNO;

	public Timestamp SendDT;

	public Timestamp OpenDT;

	public int OpenYN;

	public Timestamp OrderDT;

	public int OrderYN;

	public long OrderCD;

	public String Content = "";

	public long TemplateCD;

	public long TemplateType;

	public int DeleteYN;
}