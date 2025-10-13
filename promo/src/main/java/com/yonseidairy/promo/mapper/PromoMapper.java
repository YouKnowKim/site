package com.yonseidairy.promo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
import com.yonseidairy.promo.dao.PromoDao;

@Mapper
@Repository
public interface PromoMapper {
	
	List<AgencyDao> selectAllAgency();
	
	List<MilkbangFileDao> selectMilkbangFileList(MilkbangFileDao inMilkbangFileDao);

}
