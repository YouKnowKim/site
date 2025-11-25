package com.yonseidairy.promo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yonseidairy.promo.dao.ExtPromoDao;
import com.yonseidairy.promo.service.ExtPromoService;

@RestController
@RequestMapping("/api/extPromo")
public class ExtPromoController {

	@Autowired
	ExtPromoService extPromoService;
	
	@GetMapping("/getPromoTeamPerf")
	public List<ExtPromoDao> getPromoTeamPerf(ExtPromoDao inExtPromoDao) {
		return extPromoService.getPromoTeamPerf(inExtPromoDao);
	}
	
	@GetMapping("/getPromoPersonPerf")
	public List<ExtPromoDao> getPromoPersonPerf(ExtPromoDao inExtPromoDao) {
		return extPromoService.getPromoPersonPerf(inExtPromoDao);
	}
}
