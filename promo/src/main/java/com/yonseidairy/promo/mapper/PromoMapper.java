package com.yonseidairy.promo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
import com.yonseidairy.promo.dao.PromoDao;
import com.yonseidairy.promo.dao.TeamPersonDao;

@Mapper
@Repository
public interface PromoMapper {
	
	List<AgencyDao> selectAllAgency();
	
	List<MilkbangFileDao> selectAllMilkbangFileList();
	
	Integer selectCountMilkbangFile(MilkbangFileDao inMilkbangFileDao);
	
	List<MilkbangFileDao> selectMilkbangFileList(MilkbangFileDao inMilkbangFileDao);
	
	List<TeamPersonDao> selectAllTeamPerson();
	
	List<MilkbangFileDao> selectMilkNotSubmitFile(MilkbangFileDao inMilkbangFileDao);
	
	Integer insertMilkbangFile(MilkbangFileDao inMilkbangFileDao);
	
	Integer updateMilkbangFile(MilkbangFileDao inMilkbangFileDao);

}
