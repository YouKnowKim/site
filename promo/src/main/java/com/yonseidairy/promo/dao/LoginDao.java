package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class LoginDao {

	private String teamPersonCd;
	private String loginId;
	private String loginPw;
	private String teamPersonNm;
	private String teamPersonType;
	private String managerYn;
	private String teamCd;
	private String promoTeamCd;
	private String agencyYn;
	private String agencyCd;
	private String loginYn;
	private String loginIp;
	private String loginBrowser;
	private String status; // 0:아이디 없음, 1:비밀번호 틀림, 2:성공
	private String msg;
}
