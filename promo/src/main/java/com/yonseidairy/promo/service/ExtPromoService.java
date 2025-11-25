package com.yonseidairy.promo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonseidairy.promo.dao.ExtPromoDao;
import com.yonseidairy.promo.mapper.ExtPromoMapper;

@Service
public class ExtPromoService {
	
	@Autowired
	ExtPromoMapper extPromoMapper;
	
	// 판촉팀 월별 실적
	public List<ExtPromoDao> getPromoTeamPerf(ExtPromoDao inExtPromoDao) {
				
		return extPromoMapper.selectPromoTeamPerf(inExtPromoDao);
	}
	
	// 판촉팀원 월별 실적
	public List<ExtPromoDao> getPromoPersonPerf(ExtPromoDao inExtPromoDao) {
		
		return extPromoMapper.selectPromoPersonPerf(inExtPromoDao);
	}

}
