package com.yonseidairy.site.dao;

import lombok.Data;

@Data
public class ColdChainDao {
	
	String vendorDeviceId;
	Integer interval;
	String date;
	String time;
	String vendorId;
	String partnerId;
	
	Double temparature;
	Double humidity;
	Double light;
	String door;
	Double shock;
	Double xaxis;
	Double yaxis;
	Double zaxis;
	
	String timestamp;
	String voltage;
	String deviceType;
	
	Double longitude;
	Double latitude;
	String isValid;
	
	String deviceId;
	String refType;
	String deviceAlias;
}
