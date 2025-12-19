package com.yonseidairy.promo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.GoodsDao;
import com.yonseidairy.promo.dao.TeamDao;
import com.yonseidairy.promo.dao.TeamPersonDao;

@Mapper
@Repository
public interface SettingMapper {
	
	int existsTeamPersonCd(TeamPersonDao inTeamPersonDao);
	int insertTeamPerson(TeamPersonDao inTeamPersonDao);
	int updateTeamPerson(TeamPersonDao inTeamPersonDao);
	
	List<TeamDao> selectTeamList(TeamDao inTeamDao);
	
	List<TeamPersonDao> selectTeamPersonList(TeamPersonDao inTeamPersonDao);
	
	int updateTeamPersonPassword(TeamPersonDao inTeamPersonDao);

	/**
	 * 대리점 목록 조회
	 * 
	 * @param inAgencyDao 검색 조건 - deleteYn: 삭제여부 (0:사용, 1:미사용) - agencyCd: 대리점코드
	 *                    (부분검색) - agencyNm: 대리점명 (부분검색) - teamPersonNm: 담당자명 (부분검색)
	 * @return List<AgencyDao> 대리점 목록 - 대리점 정보, 담당자 정보, 고객 정보 포함 - AGENCYTYPE='1'인
	 *         대리점만 조회 - AGENCYCD 순으로 정렬
	 */
	List<AgencyDao> selectAgencyList(AgencyDao inAgencyDao);

	/**
	 * 대리점 코드 중복 확인
	 * 
	 * @param agencyCd 대리점코드
	 * @return int 존재 개수 - 0: 존재하지 않음 (등록 가능) - 1 이상: 이미 존재함 (등록 불가)
	 */
	int existsAgencyCd(AgencyDao inAgencyDao);

	/**
	 * 대리점 신규 등록
	 * 
	 * @param inAgencyDao 대리점 정보 - agencyCd: 대리점코드 (필수) - agencyNm: 대리점명 (필수) -
	 *                    teamPersonCd: 담당자코드 (필수)
	 * @return int 등록 건수 (성공 시 1)
	 * @description AGENCYTYPE은 1(대리점)로 고정 DELETEYN은 0(사용)으로 고정
	 */
	int insertAgency(AgencyDao inAgencyDao);

	/**
	 * 대리점 정보 수정
	 * 
	 * @param inAgencyDao 대리점 정보 - agencyCd: 대리점코드 (조건, 필수) - agencyNm: 대리점명 (수정 대상)
	 *                    - teamPersonCd: 담당자코드 (수정 대상)
	 * @return int 수정 건수 (성공 시 1)
	 */
	int updateAllAgencyName(AgencyDao inAgencyDao);
	int updateAgency(AgencyDao inAgencyDao);
	
	List<GoodsDao> selectGoodsList(GoodsDao inGoodsDao);
	int existsGoodsOptionCd(GoodsDao inGoodsDao);
	int insertGoods(GoodsDao inGoodsDao);
	int updateGoods(GoodsDao inGoodsDao);

}
