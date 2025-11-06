package com.yonseidairy.promo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.GoodsDao;
import com.yonseidairy.promo.service.SettingService;

@RestController
@RequestMapping("/api/setting")
public class SettingController {

	@Autowired
	SettingService settingService;

	@GetMapping("/getGoodsList")
	public List<GoodsDao> getGoodsList(@ModelAttribute GoodsDao inGoodsDao) {

		return settingService.getGoodsList(inGoodsDao);
	}

	@GetMapping("/getAgencyList")
	public List<AgencyDao> getAgencyList(@ModelAttribute AgencyDao inAgencyDao) {

		return settingService.getAgencyList(inAgencyDao);
	}

	@PostMapping("/saveAgencyList")
	public ResponseEntity<Map<String, Object>> saveAgencyList(@RequestBody List<AgencyDao> dataList) {

		try {
			System.out.println("=== 대리점 정보 저장 시작 ===");
			System.out.println("변경된 데이터 개수: " + (dataList != null ? dataList.size() : 0));

			// ✅ 1. 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("저장할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "저장할 데이터가 없습니다.");
				errorResponse.put("savedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// ✅ 2. changeType별 데이터 분리 (로그 출력용)
			long insertCount = dataList.stream().filter(data -> "INSERT".equals(data.getChangeType())).count();
			long updateCount = dataList.stream().filter(data -> "UPDATE".equals(data.getChangeType())).count();

			System.out.println("신규 추가(INSERT): " + insertCount + "건");
			System.out.println("수정(UPDATE): " + updateCount + "건");

			// ✅ 3. 대리점코드 유효성 검증 (신규 데이터만)
			for (AgencyDao data : dataList) {
				if ("INSERT".equals(data.getChangeType())) {
					// 대리점코드가 비어있거나 5자리가 아닌 경우
					if (data.getAgencyCd() == null || data.getAgencyCd().trim().isEmpty()
							|| !data.getAgencyCd().matches("\\d{5}")) {

						System.out.println("유효하지 않은 대리점코드: " + data.getAgencyCd());

						Map<String, Object> errorResponse = new HashMap<>();
						errorResponse.put("success", false);
						errorResponse.put("message", "대리점코드는 숫자 5자리여야 합니다. (입력값: " + data.getAgencyCd() + ")");
						errorResponse.put("savedCount", 0);

						return ResponseEntity.badRequest().body(errorResponse);
					}
				}
			}

			// ✅ 4. 서비스 레이어 호출
			int savedCount = settingService.saveAgencyList(dataList);

			System.out.println("저장 완료 - 성공: " + savedCount + "건");
			System.out.println("=== 대리점 정보 저장 완료 ===");

			// ✅ 5. 성공 응답 (Map)
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", savedCount + "건의 데이터가 성공적으로 저장되었습니다.");
			response.put("savedCount", savedCount);
			response.put("insertCount", insertCount); // 추가 정보
			response.put("updateCount", updateCount); // 추가 정보

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: " + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (DataIntegrityViolationException e) {
			// ✅ DB 제약 조건 위반 (중복 키 등)
			System.out.println("데이터 무결성 제약 위반: " + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "이미 존재하는 대리점코드이거나 데이터 무결성 제약을 위반했습니다.");
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(409).body(errorResponse); // 409 Conflict

		} catch (Exception e) {
			System.out.println("대리점 정보 저장 중 오류 발생: " + e.getMessage());
			e.printStackTrace();

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	/**
	 * 제품 목록 일괄 저장 (신규 등록 및 수정)
	 * 
	 * @param dataList 저장할 제품 목록 (JSON 배열)
	 * @return ResponseEntity<Map<String, Object>> 저장 결과
	 */
	@PostMapping("/saveGoodsList")
	public ResponseEntity<Map<String, Object>> saveGoodsList(@RequestBody List<GoodsDao> dataList) {

		try {
			System.out.println("=== 제품 정보 저장 시작 ===");
			System.out.println("변경된 데이터 개수: " + (dataList != null ? dataList.size() : 0));

			// ✅ 1. 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("저장할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "저장할 데이터가 없습니다.");
				errorResponse.put("savedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// ✅ 2. changeType별 데이터 분리 (로그 출력용)
			long insertCount = dataList.stream().filter(data -> "INSERT".equals(data.getChangeType())).count();
			long updateCount = dataList.stream().filter(data -> "UPDATE".equals(data.getChangeType())).count();

			System.out.println("신규 추가(INSERT): " + insertCount + "건");
			System.out.println("수정(UPDATE): " + updateCount + "건");

			// ✅ 3. 제품코드 유효성 검증 (신규 데이터만)
			for (GoodsDao data : dataList) {
				if ("INSERT".equals(data.getChangeType())) {
					// 제품코드가 비어있거나 숫자가 아닌 경우
					if (data.getGoodsOptionCd() == null || data.getGoodsOptionCd().trim().isEmpty()
							|| !data.getGoodsOptionCd().matches("\\d+")) {

						System.out.println("유효하지 않은 제품코드: " + data.getGoodsOptionCd());

						Map<String, Object> errorResponse = new HashMap<>();
						errorResponse.put("success", false);
						errorResponse.put("message", "제품코드는 숫자만 입력 가능합니다. (입력값: " + data.getGoodsOptionCd() + ")");
						errorResponse.put("savedCount", 0);

						return ResponseEntity.badRequest().body(errorResponse);
					}
				}
			}

			// ✅ 4. 서비스 레이어 호출
			int savedCount = settingService.saveGoodsList(dataList);

			System.out.println("저장 완료 - 성공: " + savedCount + "건");
			System.out.println("=== 제품 정보 저장 완료 ===");

			// ✅ 5. 성공 응답 (Map)
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", savedCount + "건의 데이터가 성공적으로 저장되었습니다.");
			response.put("savedCount", savedCount);
			response.put("insertCount", insertCount); // 추가 정보
			response.put("updateCount", updateCount); // 추가 정보

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: " + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (DataIntegrityViolationException e) {
			// ✅ DB 제약 조건 위반 (중복 키 등)
			System.out.println("데이터 무결성 제약 위반: " + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "이미 존재하는 제품코드이거나 데이터 무결성 제약을 위반했습니다.");
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(409).body(errorResponse); // 409 Conflict

		} catch (Exception e) {
			System.out.println("제품 정보 저장 중 오류 발생: " + e.getMessage());
			e.printStackTrace();

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}
}
