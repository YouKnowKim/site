package com.yonseidairy.promo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.promo.dao.AgencyDao;

@Mapper
@Repository
public interface SettingMapper {

	List<AgencyDao> selectAgencyList(AgencyDao inAgencyDao);
	
}
