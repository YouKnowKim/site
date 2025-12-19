package com.yonseidairy.promo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yonseidairy.promo.dao.LoginDao;
import com.yonseidairy.promo.mapper.LoginMapper;
import com.yonseidairy.promo.util.PasswordHashUtil; // ✅ 변경

@Service
public class LoginService {

	@Autowired
	LoginMapper loginMapper;

	/**
	 * 로그인 정보 조회 및 검증 - 해시 비교 방식으로 비밀번호 검증
	 * 
	 * @param inLoginDao 입력된 로그인 정보 (ID, PW - 평문)
	 * @return 로그인 결과 (status: 0-계정없음, 1-비밀번호틀림, 2-성공)
	 */
	public LoginDao getLoginInfo(LoginDao inLoginDao) {

		// 1. DB에서 계정 조회 (ID로만 조회)
		LoginDao loginTmp = loginMapper.selectLoginInfo(inLoginDao);

		// 2. 계정이 존재하지 않는 경우
		if (loginTmp == null) {
			LoginDao result = new LoginDao();
			result.setStatus("0"); // 계정 없음
			return result;
		}

		// 3. 비밀번호가 없는 경우
		if (loginTmp.getLoginPw() == null || loginTmp.getLoginPw().isEmpty()) {
			LoginDao result = new LoginDao();
			result.setStatus("0"); // 계정 없음 처리
			return result;
		}

		// ✅ 4. 비밀번호 검증 (해시 비교)
		// - inLoginDao.getLoginPw(): 사용자 입력 (평문)
		// - loginTmp.getLoginPw(): DB 저장값 (해시)
		boolean isPasswordValid = PasswordHashUtil.verify(inLoginDao.getLoginPw(), // 평문
				loginTmp.getLoginPw() // 해시
		);

		// 5. 비밀번호 비교 결과 처리
		if (isPasswordValid) {
			// 비밀번호 일치 - 로그인 성공
			loginTmp.setStatus("2");
			loginTmp.setLoginPw(inLoginDao.getLoginPw()); // ✅ 평문 반환 (세션 저장용)
			loginMapper.updateIpBrowser(inLoginDao);

		} else {
			// 비밀번호 불일치
			loginTmp.setStatus("1");
			loginMapper.updateIpBrowser(inLoginDao);
		}

		return loginTmp;
	}

	/**
	 * 비밀번호 변경 - 새 비밀번호를 해시하여 저장
	 * 
	 * @param inLoginDao 로그인 정보 (ID, 새 비밀번호 - 평문)
	 * @return 변경 결과 (msg: "s"-성공, "f"-실패)
	 */
	public LoginDao changePassword(LoginDao inLoginDao) {

		LoginDao outLoginDao = new LoginDao();

		// ✅ 비밀번호 해시 처리
		inLoginDao.setLoginPw(PasswordHashUtil.hash(inLoginDao.getLoginPw()));

		if (loginMapper.updatePassword(inLoginDao) != 0) {
			outLoginDao.setMsg("s");
		} else {
			outLoginDao.setMsg("f");
		}

		return outLoginDao;
	}
}