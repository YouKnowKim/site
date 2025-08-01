package com.yonseidairy.site.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yonseidairy.site.dao.AddressDao;
import com.yonseidairy.site.service.AddressService;

@RestController
@RequestMapping("/milk")
public class PublicUserController {
	
	@Autowired
	AddressService addressService;
	
	// 주소 기반 대리점 정보 조회 API
	@GetMapping("/getRegionByAddress")
	public HashMap<String, String> getRegionByAddress(@ModelAttribute AddressDao inAddressDao) {
		return addressService.getRegionByAddress(inAddressDao);
    }

}
