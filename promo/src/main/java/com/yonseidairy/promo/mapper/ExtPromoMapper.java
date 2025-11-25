package com.yonseidairy.promo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.promo.dao.ExtPromoDao;

@Mapper
@Repository
public interface ExtPromoMapper {
	
	// 판촉팀별 실적 조회
	public List<ExtPromoDao> selectPromoTeamPerf(ExtPromoDao inExtPromoDao);
	
	// 판촉팀원 실적 조회
	public List<ExtPromoDao> selectPromoPersonPerf(ExtPromoDao inExtPromoDao);
}
