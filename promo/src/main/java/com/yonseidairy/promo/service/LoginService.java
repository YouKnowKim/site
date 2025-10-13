package com.yonseidairy.promo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonseidairy.promo.dao.LoginDao;
import com.yonseidairy.promo.mapper.LoginMapper;

@Service
public class LoginService {

	@Autowired
	LoginMapper loginMapper;
	
	public LoginDao getLoginInfo(LoginDao inLoginDao) {
		
		LoginDao loginTmp = new LoginDao();
		
		loginTmp = loginMapper.selectLoginInfo(inLoginDao);
		
		// 계정 조회가 안되는 경우 0 반환
		if(loginTmp == null) {
			
			loginTmp = new LoginDao();
			
			loginTmp.setStatus("0");
			
		// 계정 조회가 되고, 비밀번호가 같은 경우
		} else if (loginTmp.getLoginPw().equals(inLoginDao.getLoginPw())) {
			
			loginTmp.setStatus("2");
			
			loginMapper.updateIpBrowser(inLoginDao);
			
		//	비밀번호 틀림
		} else {
			
			loginTmp.setStatus("1");
			
			loginMapper.updateIpBrowser(inLoginDao);
		}
		
		return loginTmp;
	}
	
	public LoginDao changePassword(LoginDao inLoginDao) {
		
		LoginDao outLoginDao = new LoginDao();
		
		if(loginMapper.updatePassword(inLoginDao) != 0) {
			outLoginDao.setMsg("s");
		} else {
			outLoginDao.setMsg("f");
		}
		
		return outLoginDao;
	}
}
