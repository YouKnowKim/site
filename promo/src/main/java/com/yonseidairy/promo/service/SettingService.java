package com.yonseidairy.promo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.mapper.SettingMapper;

@Service
public class SettingService {
	
	@Autowired
	SettingMapper settingMapper;
	
	public List<AgencyDao> getAgencyList(AgencyDao inAgencyDao) {
		
		return settingMapper.selectAgencyList(inAgencyDao);
	}

}
