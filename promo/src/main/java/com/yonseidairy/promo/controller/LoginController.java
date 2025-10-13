package com.yonseidairy.promo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yonseidairy.promo.dao.LoginDao;
import com.yonseidairy.promo.service.LoginService;

@RestController
@RequestMapping("/api/login")
public class LoginController {
	
	@Autowired
	LoginService loginService;

	@GetMapping("/getLoginInfo")
	public LoginDao getLoginInfo(@ModelAttribute LoginDao inLoginDao) {
		
		LoginDao outLoginDao = loginService.getLoginInfo(inLoginDao); 
		
		return outLoginDao;
	}
	
	@PostMapping("/changePassword")
	public LoginDao changePassword(@RequestBody LoginDao inLoginDao) {
		
		return loginService.changePassword(inLoginDao); 
	}
	
}
