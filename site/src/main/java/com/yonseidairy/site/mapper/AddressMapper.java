package com.yonseidairy.site.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.site.dao.ColdChainDao;
import com.yonseidairy.site.dao.InfoByCustnoDao;
import com.yonseidairy.site.dao.RegionDao;

@Mapper
@Repository
public interface AddressMapper {
	
	List<RegionDao> selectAllRegionsForAddress();
	
	InfoByCustnoDao selectInfoByCustno(InfoByCustnoDao inInfoDao);
	
	List<InfoByCustnoDao> selectCustnoList();
	
	Integer insertColdChainData(ColdChainDao inColdChainDao);

}
