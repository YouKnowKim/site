package com.yonseidairy.promo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
import com.yonseidairy.promo.mapper.PromoMapper;

@Service
public class PromoService {

	@Autowired
	PromoMapper promoMapper;
	
	public List<AgencyDao> getAllAgency(AgencyDao inAgencyDao) {
		
		return promoMapper.selectAllAgency();
	}
	
	public List<MilkbangFileDao> getMilkbangFileList(MilkbangFileDao inMilkbangFileDao) {
		
		return promoMapper.selectMilkbangFileList(inMilkbangFileDao);
	}
}
