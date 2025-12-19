package com.yonseidairy.promo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.GoodsDao;
import com.yonseidairy.promo.dao.TeamDao;
import com.yonseidairy.promo.dao.TeamPersonDao;
import com.yonseidairy.promo.mapper.SettingMapper;
import com.yonseidairy.promo.util.PasswordHashUtil;

@Service
public class SettingService {

	@Autowired
	SettingMapper settingMapper;

	public List<TeamDao> getTeamList(TeamDao inTeamDao) {

		return settingMapper.selectTeamList(inTeamDao);
	}

	public List<TeamPersonDao> getTeamPersonList(TeamPersonDao inTeamPersonDao) {
		
		List<TeamPersonDao> arrList = settingMapper.selectTeamPersonList(inTeamPersonDao);

		return arrList;
	}

	public List<GoodsDao> getGoodsList(GoodsDao inGoodsDao) {

		return settingMapper.selectGoodsList(inGoodsDao);
	}

	public List<AgencyDao> getAgencyList(AgencyDao inAgencyDao) {

		return settingMapper.selectAgencyList(inAgencyDao);
	}

	public int insertAgency(AgencyDao inAgencyDao) {

		return settingMapper.insertAgency(inAgencyDao);
	}

	public int updateAgency(AgencyDao inAgencyDao) {

		return settingMapper.updateAgency(inAgencyDao);
	}
	
	public int updateAllAgencyName(AgencyDao inAgencyDao) {

		return settingMapper.updateAllAgencyName(inAgencyDao);
	}
	
	/**
	 * 사원 비밀번호 초기화 (1234로 설정)
	 * - 선택된 사원들의 비밀번호를 초기 비밀번호로 일괄 초기화
	 * 
	 * @param teamPersonCdList 초기화할 사원코드 목록
	 * @return int 업데이트된 총 건수
	 */
	@Transactional
	public int resetTeamPersonPassword(List<String> teamPersonCdList) {
	    
	    int updatedCount = 0;
	    
	    // ✅ 초기 비밀번호 '1234' 해시 처리
	    String hashedPassword = PasswordHashUtil.hash("1234");
	    
	    // ✅ 각 사원코드에 대해 비밀번호 초기화 수행
	    for (String teamPersonCd : teamPersonCdList) {
	        
	        TeamPersonDao dao = new TeamPersonDao();
	        dao.setTeamPersonCd(teamPersonCd);
	        dao.setLoginPw(hashedPassword);
	        
	        int result = settingMapper.updateTeamPersonPassword(dao);
	        updatedCount += result;
	        
	        System.out.println("비밀번호 초기화 - 사원코드: " + teamPersonCd);
	    }
	    
	    return updatedCount;
	}

	/**
	 * 사원 목록 일괄 저장 (신규 등록 및 수정)
	 * 
	 * @param dataList 저장할 사원 목록 - changeType: "INSERT" 또는 "UPDATE"
	 * @return int 저장된 총 건수
	 * @throws IllegalArgumentException 중복된 사원코드 또는 유효하지 않은 changeType인 경우
	 */
	@Transactional
	public int saveTeamPersonList(List<TeamPersonDao> dataList) {

		int savedCount = 0;

		for (TeamPersonDao data : dataList) {

			// ✅ changeType에 따라 INSERT 또는 UPDATE 수행
			if ("INSERT".equals(data.getChangeType())) {

				// ✅ 신규 추가
				System.out.println("INSERT - 사원코드: " + data.getTeamPersonCd());

				// ✅ 중복 체크
//				int existingTeamPerson = settingMapper.existsTeamPersonCd(data);
//				if (existingTeamPerson != 0) {
//					throw new IllegalArgumentException("이미 존재하는 사원코드입니다: " + data.getTeamPersonCd());
//				}

				// ✅ INSERT 실행
				int insertResult = settingMapper.insertTeamPerson(data);
				savedCount += insertResult;

			} else if ("UPDATE".equals(data.getChangeType())) {

				// ✅ 수정
				System.out.println("UPDATE - 사원코드: " + data.getTeamPersonCd());
				
				data.setLoginPw(PasswordHashUtil.hash(data.getLoginPw()));

				// ✅ UPDATE 실행
				int updateResult = settingMapper.updateTeamPerson(data);
				savedCount += updateResult;

			} else {
				// ✅ changeType이 없거나 알 수 없는 경우
				System.out.println("알 수 없는 changeType: " + data.getChangeType());
				throw new IllegalArgumentException("유효하지 않은 변경 유형입니다: " + data.getChangeType());
			}
		}

		return savedCount;
	}

	@Transactional
	public int saveAgencyList(List<AgencyDao> dataList) {

		int savedCount = 0;

		for (AgencyDao data : dataList) {

			// ✅ changeType에 따라 INSERT 또는 UPDATE 수행
			if ("INSERT".equals(data.getChangeType())) {

				// 신규 추가
				System.out.println("INSERT - 대리점코드: " + data.getAgencyCd());

				// 중복 체크 (선택사항)
				int existingAgency = settingMapper.existsAgencyCd(data);
				if (existingAgency != 0) {
					throw new IllegalArgumentException("이미 존재하는 대리점코드입니다: " + data.getAgencyCd());
				}

				// INSERT 실행
				int insertResult = settingMapper.insertAgency(data);
				savedCount += insertResult;

			} else if ("UPDATE".equals(data.getChangeType())) {

				// 수정
				System.out.println("UPDATE - 대리점코드: " + data.getAgencyCd());

				// UPDATE 실행
				int updateResult = settingMapper.updateAgency(data);
				savedCount += updateResult;

			} else {
				// changeType이 없거나 알 수 없는 경우
				System.out.println("알 수 없는 changeType: " + data.getChangeType());
				throw new IllegalArgumentException("유효하지 않은 변경 유형입니다: " + data.getChangeType());
			}
		}

		return savedCount;
	}

	/**
	 * 제품 목록 일괄 저장 (신규 등록 및 수정)
	 *
	 * @param dataList 저장할 제품 목록 - changeType: "INSERT" 또는 "UPDATE"
	 * @return int 저장된 총 건수
	 * @throws IllegalArgumentException 중복된 제품코드 또는 유효하지 않은 changeType인 경우
	 */
	@Transactional
	public int saveGoodsList(List<GoodsDao> dataList) {

		int savedCount = 0;

		for (GoodsDao data : dataList) {

			// ✅ changeType에 따라 INSERT 또는 UPDATE 수행
			if ("INSERT".equals(data.getChangeType())) {

				// 신규 추가
				System.out.println("INSERT - 제품코드: " + data.getGoodsOptionCd());

				// 중복 체크
				int existingGoods = settingMapper.existsGoodsOptionCd(data);
				if (existingGoods != 0) {
					throw new IllegalArgumentException("이미 존재하는 제품코드입니다: " + data.getGoodsOptionCd());
				}

				// INSERT 실행
				int insertResult = settingMapper.insertGoods(data);
				savedCount += insertResult;

			} else if ("UPDATE".equals(data.getChangeType())) {

				// 수정
				System.out.println("UPDATE - 제품코드: " + data.getGoodsOptionCd());

				// UPDATE 실행
				int updateResult = settingMapper.updateGoods(data);
				savedCount += updateResult;

			} else {
				// changeType이 없거나 알 수 없는 경우
				System.out.println("알 수 없는 changeType: " + data.getChangeType());
				throw new IllegalArgumentException("유효하지 않은 변경 유형입니다: " + data.getChangeType());
			}
		}

		return savedCount;
	}

}
