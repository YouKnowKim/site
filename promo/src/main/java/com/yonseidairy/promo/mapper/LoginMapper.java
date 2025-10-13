package com.yonseidairy.promo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.promo.dao.LoginDao;

@Mapper
@Repository
public interface LoginMapper {
	
	LoginDao selectLoginInfo(LoginDao inLoginDao);
	
	Integer updateIpBrowser(LoginDao inLoginDao);
	
	Integer updatePassword(LoginDao inLoginDao);
}
