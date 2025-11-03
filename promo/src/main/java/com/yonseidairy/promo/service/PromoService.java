package com.yonseidairy.promo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangDetailDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
import com.yonseidairy.promo.dao.PromoTeamDao;
import com.yonseidairy.promo.dao.TeamPersonDao;
import com.yonseidairy.promo.mapper.PromoMapper;

@Service
public class PromoService {

	@Autowired
	PromoMapper promoMapper;

	public List<AgencyDao> getAllAgency(AgencyDao inAgencyDao) {

		return promoMapper.selectAllAgency();
	}

	public List<AgencyDao> getMyAgencyList(AgencyDao inAgencyDao) {

		return promoMapper.selectMyAgencyList(inAgencyDao);
	}

	public List<MilkbangFileDao> getAllMilkbangFileList() {

		return promoMapper.selectAllMilkbangFileList();
	}

	public List<MilkbangFileDao> getMilkbangFileList(MilkbangFileDao inMilkbangFileDao) {

		return promoMapper.selectMilkbangFileList(inMilkbangFileDao);
	}

	public List<TeamPersonDao> getAllTeamPerson() {

		return promoMapper.selectAllTeamPerson();
	}

	public List<MilkbangFileDao> getMilkNotSubmitFileList(MilkbangFileDao inMilkbangFileDao) {

		return promoMapper.selectMilkNotSubmitFile(inMilkbangFileDao);
	}

	public List<MilkbangDetailDao> getMilkbangDetailList(MilkbangDetailDao inMilkbangDetailDao) {

		return promoMapper.selectMilkbangDetailList(inMilkbangDetailDao);
	}

	public List<MilkbangDetailDao> getMilkbangDetail(MilkbangDetailDao inMilkbangDetailDao) {

		return promoMapper.selectMilkbangDetail(inMilkbangDetailDao);
	}

	public List<PromoTeamDao> getAllPromoTeam(PromoTeamDao inPromoTeamDao) {

		return promoMapper.selectAllPromoTeam(inPromoTeamDao);
	}

	/**
	 * 판촉실적 저장 (사전 검증 + 전체 저장 or 전체 취소)
	 * - 저장 전 모든 데이터의 masterCloseYn 확인
	 * - 하나라도 마감 데이터가 있으면 전체 저장 취소
	 */
	@Transactional(rollbackFor = Exception.class)
	public Integer savePromo(List<MilkbangDetailDao> dataList) throws Exception {

		if (dataList == null || dataList.isEmpty()) {
			throw new IllegalArgumentException("저장할 데이터가 없습니다.");
		}

		System.out.println("=== 저장 가능 여부 사전 검증 시작 ===");

		// ✅ 1단계: 모든 데이터의 저장 가능 여부 사전 검증
		List<String> unsavableList = new ArrayList<>();

		for (MilkbangDetailDao data : dataList) {
			// 필수 값 체크
			if (data.getOrderCd() == null || data.getOrderCd().isEmpty() || 
			    data.getOrderSeq() == null || data.getOrderSeq().isEmpty()) {
				throw new IllegalArgumentException("필수 값(orderCd, orderSeq)이 누락되었습니다.");
			}

			// DB에서 저장 가능 여부 확인
			MilkbangDetailDao checkResult = promoMapper.checkSavable(data);

			// 데이터가 존재하지 않는 경우
			if (checkResult == null) {
				String msg = "데이터를 찾을 수 없음 - orderCd: " + data.getOrderCd() + 
				            ", orderSeq: " + data.getOrderSeq();
				System.out.println(msg);
				unsavableList.add(data.getOrderCd() + "-" + data.getOrderSeq() + " (데이터 없음)");
				continue;
			}

			// ✅ masterCloseYn이 1이면 저장 불가
			String masterCloseYn = checkResult.getMasterCloseYn();
			String status = checkResult.getStatus();

			boolean isClosed = "1".equals(masterCloseYn);

			if (isClosed) {
				String msg = "저장 불가 - orderCd: " + data.getOrderCd() + 
				            ", orderSeq: " + data.getOrderSeq() + ", 상태: " + status;
				System.out.println(msg);
				unsavableList.add(data.getOrderCd() + "-" + data.getOrderSeq() + " (" + status + ")");
			}
		}

		// ✅ 2단계: 저장 불가능한 데이터가 하나라도 있으면 전체 저장 취소
		if (!unsavableList.isEmpty()) {
			System.out.println("=== 저장 불가능한 데이터 발견 - 전체 저장 취소 ===");
			System.out.println("저장 불가 건수: " + unsavableList.size() + "건");
			unsavableList.forEach(System.out::println);

			// 상세 오류 메시지 생성
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("선택한 데이터 중 ").append(unsavableList.size()).append("건은 저장할 수 없습니다.\n\n");
			errorMsg.append("【저장 불가 목록】\n");

			int closedCount = 0;
			int notFoundCount = 0;

			for (String item : unsavableList) {
				errorMsg.append("• ").append(item).append("\n");
				if (item.contains("마감")) closedCount++;
				else if (item.contains("데이터 없음")) notFoundCount++;
			}

			errorMsg.append("\n【저장 불가 사유】\n");
			if (closedCount > 0) {
				errorMsg.append("• 마감된 데이터: ").append(closedCount).append("건\n");
			}
			if (notFoundCount > 0) {
				errorMsg.append("• 존재하지 않는 데이터: ").append(notFoundCount).append("건\n");
			}
			errorMsg.append("\n마감된 데이터는 저장할 수 없습니다.");
			errorMsg.append("\n재조회 후 미저장 또는 저장 상태의 데이터만 선택하여 다시 시도해주세요.");

			throw new IllegalArgumentException(errorMsg.toString());
		}

		System.out.println("=== 모든 데이터 저장 가능 확인 - 저장 진행 ===");

		// ✅ 3단계: 모든 데이터가 저장 가능한 경우에만 실제 저장 진행
		Integer result = 0;

		for (MilkbangDetailDao data : dataList) {
			try {
				int updateResult = promoMapper.mergePromo(data);
				
				if (updateResult == 0) {
					// 마감된 데이터는 업데이트되지 않음 (이미 사전 검증 완료)
					System.out.println("예상치 못한 저장 실패 - orderCd: " + data.getOrderCd() + 
					                 ", orderSeq: " + data.getOrderSeq());
					throw new RuntimeException("저장 중 예상치 못한 오류가 발생했습니다.");
				}
				
				result++;
				System.out.println("저장 완료 - orderCd: " + data.getOrderCd() + 
				                 ", orderSeq: " + data.getOrderSeq());
				
			} catch (Exception e) {
				System.out.println("저장 실패 - orderCd: " + data.getOrderCd() + 
				                 ", orderSeq: " + data.getOrderSeq() + ", 오류: " + e.getMessage());
				e.printStackTrace();
				throw new RuntimeException("데이터 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
			}
		}

		System.out.println("=== 판촉실적 저장 완료 ===");
		System.out.println("저장 성공: " + result + "건");

		return result;
	}

	/**
	 * 판촉실적 상세 저장 (사전 검증 + 전체 저장 or 전체 취소)
	 * - 저장 전 모든 데이터의 masterCloseYn 확인
	 * - 하나라도 마감 데이터가 있으면 전체 저장 취소
	 */
	@Transactional(rollbackFor = Exception.class)
	public Integer savePromoDetail(List<MilkbangDetailDao> dataList) throws Exception {

		if (dataList == null || dataList.isEmpty()) {
			throw new IllegalArgumentException("저장할 데이터가 없습니다.");
		}

		System.out.println("=== 저장 가능 여부 사전 검증 시작 ===");

		// ✅ 1단계: 모든 데이터의 저장 가능 여부 사전 검증
		List<String> unsavableList = new ArrayList<>();

		for (MilkbangDetailDao data : dataList) {
			// 필수 값 체크
			if (data.getOrderCd() == null || data.getOrderCd().isEmpty() || 
			    data.getOrderSeq() == null || data.getOrderSeq().isEmpty()) {
				throw new IllegalArgumentException("필수 값(orderCd, orderSeq)이 누락되었습니다.");
			}

			// DB에서 저장 가능 여부 확인
			MilkbangDetailDao checkResult = promoMapper.checkSavable(data);

			// 데이터가 존재하지 않는 경우
			if (checkResult == null) {
				String msg = "데이터를 찾을 수 없음 - orderCd: " + data.getOrderCd() + 
				            ", orderSeq: " + data.getOrderSeq();
				System.out.println(msg);
				unsavableList.add(data.getOrderCd() + "-" + data.getOrderSeq() + " (데이터 없음)");
				continue;
			}

			// ✅ masterCloseYn이 1이면 저장 불가
			String masterCloseYn = checkResult.getMasterCloseYn();
			String status = checkResult.getStatus();

			boolean isClosed = "1".equals(masterCloseYn);

			if (isClosed) {
				String msg = "저장 불가 - orderCd: " + data.getOrderCd() + 
				            ", orderSeq: " + data.getOrderSeq() + ", 상태: " + status;
				System.out.println(msg);
				unsavableList.add(data.getOrderCd() + "-" + data.getOrderSeq() + " (" + status + ")");
			}
		}

		// ✅ 2단계: 저장 불가능한 데이터가 하나라도 있으면 전체 저장 취소
		if (!unsavableList.isEmpty()) {
			System.out.println("=== 저장 불가능한 데이터 발견 - 전체 저장 취소 ===");
			System.out.println("저장 불가 건수: " + unsavableList.size() + "건");
			unsavableList.forEach(System.out::println);

			// 상세 오류 메시지 생성
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("선택한 데이터 중 ").append(unsavableList.size()).append("건은 저장할 수 없습니다.\n\n");
			errorMsg.append("【저장 불가 목록】\n");

			int closedCount = 0;
			int notFoundCount = 0;

			for (String item : unsavableList) {
				errorMsg.append("• ").append(item).append("\n");
				if (item.contains("마감")) closedCount++;
				else if (item.contains("데이터 없음")) notFoundCount++;
			}

			errorMsg.append("\n【저장 불가 사유】\n");
			if (closedCount > 0) {
				errorMsg.append("• 마감된 데이터: ").append(closedCount).append("건\n");
			}
			if (notFoundCount > 0) {
				errorMsg.append("• 존재하지 않는 데이터: ").append(notFoundCount).append("건\n");
			}
			errorMsg.append("\n마감된 데이터는 저장할 수 없습니다.");
			errorMsg.append("\n재조회 후 미저장 또는 저장 상태의 데이터만 선택하여 다시 시도해주세요.");

			throw new IllegalArgumentException(errorMsg.toString());
		}

		System.out.println("=== 모든 데이터 저장 가능 확인 - 저장 진행 ===");

		// ✅ 3단계: 모든 데이터가 저장 가능한 경우에만 실제 저장 진행
		Integer result = 0;

		for (MilkbangDetailDao data : dataList) {
			try {
				int updateResult = promoMapper.mergePromo(data);
				
				if (updateResult == 0) {
					System.out.println("예상치 못한 저장 실패 - orderCd: " + data.getOrderCd() + 
					                 ", orderSeq: " + data.getOrderSeq());
					throw new RuntimeException("저장 중 예상치 못한 오류가 발생했습니다.");
				}
				
				promoMapper.mergePromoDetail(data);
				result++;
				
				System.out.println("저장 완료 - orderCd: " + data.getOrderCd() + 
				                 ", orderSeq: " + data.getOrderSeq());
				
			} catch (Exception e) {
				System.out.println("저장 실패 - orderCd: " + data.getOrderCd() + 
				                 ", orderSeq: " + data.getOrderSeq() + ", 오류: " + e.getMessage());
				e.printStackTrace();
				throw new RuntimeException("데이터 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
			}
		}

		System.out.println("=== 판촉실적 상세 저장 완료 ===");
		System.out.println("저장 성공: " + result + "건");

		return result;
	}

	/**
	 * 판촉실적 삭제 (사전 검증 + 전체 삭제 or 전체 취소) - 삭제 전 모든 데이터의 saveYn, masterCloseYn 확인 -
	 * 하나라도 저장/마감 데이터가 있으면 전체 삭제 취소
	 */
	@Transactional(rollbackFor = Exception.class)
	public int deletePromo(List<MilkbangDetailDao> dataList) {

		if (dataList == null || dataList.isEmpty()) {
			throw new IllegalArgumentException("삭제할 데이터가 없습니다.");
		}

		System.out.println("=== 삭제 가능 여부 사전 검증 시작 ===");

		// ✅ 1단계: 모든 데이터의 삭제 가능 여부 사전 검증
		List<String> undeletableList = new ArrayList<>();

		for (MilkbangDetailDao data : dataList) {
			// 필수 값 체크
			if (data.getOrderCd() == null || data.getOrderCd().isEmpty() || data.getOrderSeq() == null
					|| data.getOrderSeq().isEmpty()) {
				throw new IllegalArgumentException("필수 값(orderCd, orderSeq)이 누락되었습니다.");
			}

			// DB에서 삭제 가능 여부 확인
			MilkbangDetailDao checkResult = promoMapper.checkDeletable(data);

			// 데이터가 존재하지 않는 경우
			if (checkResult == null) {
				String msg = "데이터를 찾을 수 없음 - orderCd: " + data.getOrderCd() + ", orderSeq: " + data.getOrderSeq();
				System.out.println(msg);
				undeletableList.add(data.getOrderCd() + "-" + data.getOrderSeq() + " (데이터 없음)");
				continue;
			}

			// saveYn 또는 masterCloseYn이 1이면 삭제 불가
			String saveYn = checkResult.getSaveYn();
			String masterCloseYn = checkResult.getMasterCloseYn();
			String status = checkResult.getStatus();

			boolean isSaved = "1".equals(saveYn);
			boolean isClosed = "1".equals(masterCloseYn);

			if (isSaved || isClosed) {
				String msg = "삭제 불가 - orderCd: " + data.getOrderCd() + ", orderSeq: " + data.getOrderSeq() + ", 상태: "
						+ status;
				System.out.println(msg);
				undeletableList.add(data.getOrderCd() + "-" + data.getOrderSeq() + " (" + status + ")");
			}
		}

		// ✅ 2단계: 삭제 불가능한 데이터가 하나라도 있으면 전체 삭제 취소
		if (!undeletableList.isEmpty()) {
			System.out.println("=== 삭제 불가능한 데이터 발견 - 전체 삭제 취소 ===");
			System.out.println("삭제 불가 건수: " + undeletableList.size() + "건");
			undeletableList.forEach(System.out::println);

			// 상세 오류 메시지 생성
			StringBuilder errorMsg = new StringBuilder();
			errorMsg.append("선택한 데이터 중 ").append(undeletableList.size()).append("건은 삭제할 수 없습니다.\n\n");
			errorMsg.append("【삭제 불가 목록】\n");

			int savedCount = 0;
			int closedCount = 0;
			int notFoundCount = 0;

			for (String item : undeletableList) {
				errorMsg.append("• ").append(item).append("\n");
				if (item.contains("저장"))
					savedCount++;
				else if (item.contains("마감"))
					closedCount++;
				else if (item.contains("데이터 없음"))
					notFoundCount++;
			}

			errorMsg.append("\n【삭제 불가 사유】\n");
			if (savedCount > 0) {
				errorMsg.append("• 저장된 데이터: ").append(savedCount).append("건\n");
			}
			if (closedCount > 0) {
				errorMsg.append("• 마감된 데이터: ").append(closedCount).append("건\n");
			}
			if (notFoundCount > 0) {
				errorMsg.append("• 존재하지 않는 데이터: ").append(notFoundCount).append("건\n");
			}
			errorMsg.append("\n저장되거나 마감된 데이터는 삭제할 수 없습니다.");
			errorMsg.append("\n재조회 후 미저장 상태의 데이터만 선택하여 다시 시도해주세요.");

			throw new IllegalArgumentException(errorMsg.toString());
		}

		System.out.println("=== 모든 데이터 삭제 가능 확인 - 삭제 진행 ===");

		// ✅ 3단계: 모든 데이터가 삭제 가능한 경우에만 실제 삭제 진행
		int deletedCount = 0;

		for (MilkbangDetailDao data : dataList) {
			try {
				// Detail 테이블 삭제
				int deleteResult = promoMapper.deleteMilkbangGoods(data);

				if (deleteResult == 0) {
					// 이 경우는 발생하지 않아야 함 (사전 검증 완료)
					System.out.println(
							"예상치 못한 삭제 실패 - orderCd: " + data.getOrderCd() + ", orderSeq: " + data.getOrderSeq());
					throw new RuntimeException("삭제 중 예상치 못한 오류가 발생했습니다.");
				}

				// Master 테이블 조건부 삭제
				promoMapper.deleteMilkbang(data);

				deletedCount++;
				System.out.println("삭제 완료 - orderCd: " + data.getOrderCd() + ", orderSeq: " + data.getOrderSeq());

			} catch (Exception e) {
				System.out.println("삭제 실패 - orderCd: " + data.getOrderCd() + ", orderSeq: " + data.getOrderSeq()
						+ ", 오류: " + e.getMessage());
				e.printStackTrace();
				throw new RuntimeException("데이터 삭제 중 오류가 발생했습니다: " + e.getMessage(), e);
			}
		}

		System.out.println("=== 판촉실적 삭제 완료 ===");
		System.out.println("삭제 성공: " + deletedCount + "건");

		return deletedCount;
	}

	public Integer mergeMilkbangFile(String originalFileName) throws Exception {

		String agencyCd = extractAgencyCodeFromFileName(originalFileName);
		String url = "http://milkbang.yonseidairy.com/";
		String fileUrl = url + agencyCd;

		// 파일 정보 셋팅
		MilkbangFileDao fileDao = new MilkbangFileDao();
		fileDao.setFileNm(originalFileName);
		fileDao.setAgencyCd(agencyCd);
		fileDao.setUploadYn("-1");
		fileDao.setFileUrl(fileUrl);
		fileDao.setFileStatus("4");

		// DB에 이미 등록된 파일 목록 조회
		Integer fileCount = promoMapper.selectCountMilkbangFile(fileDao);

		if (fileCount <= 0) {
			promoMapper.insertMilkbangFile(fileDao);
		} else {
			promoMapper.updateMilkbangFile(fileDao);
		}

		return 0;
	}

	/**
	 * 파일명에서 대리점 코드 추출 및 변환 예: 10001_용산_250922_0928.xls -> "001" -> 10001
	 * 12345_강남_250922_0928.xls -> "345" -> 10345
	 * 
	 * @param fileName 파일명
	 * @return 변환된 대리점 코드 (추출 실패 시 null)
	 */
	private String extractAgencyCodeFromFileName(String fileName) {
		if (fileName == null || fileName.isEmpty()) {
			return null;
		}

		try {
			// 첫 번째 underscore 위치 찾기
			int underscoreIndex = fileName.indexOf("_");

			if (underscoreIndex > 0) {
				// underscore 앞부분 추출
				String agencyCodeStr = fileName.substring(0, underscoreIndex);

				// 숫자로만 구성되어 있는지 확인
				if (agencyCodeStr.matches("\\d+")) {
					// 뒤 3자리 추출
					int length = agencyCodeStr.length();
					String last3Digits;

					if (length >= 3) {
						// 3자리 이상이면 뒤 3자리 추출
						last3Digits = agencyCodeStr.substring(length - 3);
					} else {
						// 3자리 미만이면 앞에 0을 패딩
						last3Digits = String.format("%03d", Integer.parseInt(agencyCodeStr));
					}

					// 10000을 더하기
					int agencyCodeInt = Integer.parseInt(last3Digits) + 10000;
					String agencyCode = String.valueOf(agencyCodeInt);

					System.out.println("파일명: " + fileName + " -> 추출: " + agencyCodeStr + " -> 변환: " + agencyCode);

					return agencyCode;
				}
			}
		} catch (Exception e) {
			System.err.println("파일명에서 대리점 코드 추출 실패: " + fileName + " - " + e.getMessage());
		}

		return null;
	}
}