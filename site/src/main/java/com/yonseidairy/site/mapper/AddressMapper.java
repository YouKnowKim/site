package com.yonseidairy.site.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.site.dao.RegionDao;

@Mapper
@Repository
public interface AddressMapper {
	
	List<RegionDao> selectAllRegionsForAddress(); 

}
