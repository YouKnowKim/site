package com.yonseidairy.promo.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangDetailDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
import com.yonseidairy.promo.dao.PromoCloseDao;
import com.yonseidairy.promo.dao.PromoDao;
import com.yonseidairy.promo.dao.PromoTeamDao;
import com.yonseidairy.promo.dao.TeamPersonDao;

@Mapper
@Repository
public interface PromoMapper {
	
	// 마감 내역 조회
	List<PromoCloseDao> selectCloseList(PromoCloseDao inPromoCloseDao);
	
	// 전체 대리점 목록을 조회합니다.
	List<AgencyDao> selectAllAgency();

	// 사용자 권한에 따른 담당 대리점 목록을 조회합니다.
	List<AgencyDao> selectMyAgencyList(AgencyDao inAgencyDao);

	// 전체 우유방 파일 목록을 조회합니다. (주차별 업로드된 모든 엑셀 파일 정보 포함)
	List<MilkbangFileDao> selectAllMilkbangFileList();

	// 조건에 맞는 우유방 파일의 총 개수를 조회합니다. (페이징 처리용)
	Integer selectCountMilkbangFile(MilkbangFileDao inMilkbangFileDao);

	// 조건에 맞는 우유방 파일 목록을 조회합니다. (년도, 주차, 대리점 등 필터링)
	List<MilkbangFileDao> selectMilkbangFileList(MilkbangFileDao inMilkbangFileDao);

	// 전체 팀원(배달기사) 목록을 조회합니다.
	List<TeamPersonDao> selectAllTeamPerson();

	// 미제출된 우유방 파일 목록을 조회합니다. (해당 주차에 파일 업로드 안된 대리점)
	List<MilkbangFileDao> selectMilkNotSubmitFile(MilkbangFileDao inMilkbangFileDao);

	// 우유방 파일 정보를 신규 등록합니다. (엑셀 업로드 시 파일 메타 정보 저장)
	Integer insertMilkbangFile(MilkbangFileDao inMilkbangFileDao);

	// 우유방 파일 정보를 수정합니다. (파일 재업로드 또는 상태 변경)
	Integer updateMilkbangFile(MilkbangFileDao inMilkbangFileDao);

	// 우유방 상세 정보 목록을 조회합니다. (업로드된 엑셀 파일의 상세 배달 내역)
	List<MilkbangDetailDao> selectMilkbangDetailList(MilkbangDetailDao inMilkbangDetailDao);

	// 특정 우유방 상세 정보를 조회합니다. (단건 또는 특정 조건의 상세 배달 내역)
	List<MilkbangDetailDao> selectMilkbangDetail(MilkbangDetailDao inMilkbangDetailDao);

	// 전체 프로모션 팀 목록을 조회합니다.
	List<PromoTeamDao> selectAllPromoTeam(PromoTeamDao inPromoTeamDao);

	// 프로모션 정보를 병합(Merge) 처리합니다. (존재하면 수정, 없으면 신규 등록)
	Integer mergePromo(MilkbangDetailDao inMilkbangDetailDao);

	// 프로모션 상세 정보를 병합(Merge) 처리합니다. (존재하면 수정, 없으면 신규 등록)
	Integer mergePromoDetail(MilkbangDetailDao inMilkbangDetailDao);

	// 우유방 상품 정보를 삭제합니다. (특정 조건의 배달 상품 데이터 삭제)
	int deleteMilkbangGoods(MilkbangDetailDao inMilkbangDetailDao);

	// 우유방 상품의 개수를 조회합니다. (삭제 전 데이터 존재 여부 확인용)
	int countMilkbangGoods(MilkbangDetailDao inMilkbangDetailDao);

	// 우유방 정보를 삭제합니다. (마스터 데이터 삭제, 연관 상세 데이터 함께 삭제 가능)
	int deleteMilkbang(MilkbangDetailDao inMilkbangDetailDao);

	// 우유방 데이터의 삭제 가능 여부를 확인합니다. (참조 여부, 업무 규칙 위배 체크)
	MilkbangDetailDao checkDeletable(MilkbangDetailDao inMilkbangDetailDao);

	// 우유방 데이터의 저장 가능 여부를 확인합니다. (중복 체크, 필수값 검증, 업무 규칙 체크)
	MilkbangDetailDao checkSavable(MilkbangDetailDao inMilkbangDetailDao);

}
