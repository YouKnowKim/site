package com.yonseidairy.promo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonseidairy.promo.dao.ExtPromoDao;
import com.yonseidairy.promo.mapper.ExtPromoMapper;

@Service
public class ExtPromoService {
	
	@Autowired
	ExtPromoMapper extPromoMapper;
	
	public List<ExtPromoDao> getPromoTeamPerf(ExtPromoDao inExtPromoDao) {
		return extPromoMapper.selectPromoTeamPerf(inExtPromoDao);
	}

}
