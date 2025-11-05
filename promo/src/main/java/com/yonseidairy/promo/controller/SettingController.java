package com.yonseidairy.promo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.service.SettingService;

@RestController
@RequestMapping("/api/setting")
public class SettingController {

	@Autowired
	SettingService settingService;
	
	@GetMapping("/getAgencyList")
	public List<AgencyDao> getAgencyList(@ModelAttribute AgencyDao inAgencyDao) {
		
		return settingService.getAgencyList(inAgencyDao);
	}
}
