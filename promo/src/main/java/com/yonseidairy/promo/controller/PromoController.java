package com.yonseidairy.promo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangDetailDao;
import com.yonseidairy.promo.dao.MilkbangDetailPivotDao;
import com.yonseidairy.promo.dao.MilkbangDetailTeamPivotDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
import com.yonseidairy.promo.dao.PromoCloseDao;
import com.yonseidairy.promo.dao.PromoTeamDao;
import com.yonseidairy.promo.dao.TeamPersonDao;
import com.yonseidairy.promo.service.PromoService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/api/promo")
public class PromoController {

	// 파일 저장 경로
	private static final String FILE_BASE_PATH = "C:/develop/FILES/Milkbang/판촉자료/";

	@Autowired
	PromoService promoService;

	@GetMapping("/getCloseList")
	public List<PromoCloseDao> getCloseList(@ModelAttribute PromoCloseDao inPromoCloseDao) {

		return promoService.getCloseList(inPromoCloseDao);
	}

	@GetMapping("/getAllAgency")
	public List<AgencyDao> getAllAgency(@ModelAttribute AgencyDao inAgencyDao) {

		return promoService.getAllAgency(inAgencyDao);
	}

	@GetMapping("/getMyAgencyList")
	public List<AgencyDao> getMyAgencyList(@ModelAttribute AgencyDao inAgencyDao) {

		return promoService.getMyAgencyList(inAgencyDao);
	}
	
	@GetMapping("/getHappyAgencyList")
	public List<AgencyDao> getHappyAgencyList(@ModelAttribute AgencyDao inAgencyDao) {

		return promoService.getHappyAgencyList(inAgencyDao);
	}

	@GetMapping("/getMilkbangFileList")
	public List<MilkbangFileDao> getMilkbangFileList(@ModelAttribute MilkbangFileDao inMilkbangFileDao) {

		return promoService.getMilkbangFileList(inMilkbangFileDao);
	}

	@GetMapping("/getAllTeamPerson")
	public List<TeamPersonDao> getAllTeamPerson() {

		return promoService.getAllTeamPerson();
	}
	
	@GetMapping("/getMyTeamPerson")
	public List<TeamPersonDao> getMyTeamPerson(TeamPersonDao inTeamPersonDao) {

		return promoService.getMyTeamPerson(inTeamPersonDao);
	}


	@GetMapping("/getMilkNotSubmitFileList")
	public List<MilkbangFileDao> getMilkNotSubmitFileList(@ModelAttribute MilkbangFileDao inMilkbangFileDao) {

		return promoService.getMilkNotSubmitFileList(inMilkbangFileDao);
	}

	@GetMapping("/getMilkbangDetailList")
	public List<MilkbangDetailDao> getMilkbangDetailList(@ModelAttribute MilkbangDetailDao inMilkbangDetailDao) {

		return promoService.getMilkbangDetailList(inMilkbangDetailDao);
	}
	
	@GetMapping("/getHappyMilkbangDetailList")
	public List<MilkbangDetailDao> getHappyMilkbangDetailList(@ModelAttribute MilkbangDetailDao inMilkbangDetailDao) {

		return promoService.getHappyMilkbangDetailList(inMilkbangDetailDao);
	}
	
	@GetMapping("/getMilkbangDetailListPivot")
	public List<MilkbangDetailPivotDao> getMilkbangDetailListPivot(@ModelAttribute MilkbangDetailDao inMilkbangDetailDao) {

		return promoService.getMilkbangDetailListPivot(inMilkbangDetailDao);
	}
	
	@GetMapping("/getMilkbangDetailListTeamPivot")
	public List<MilkbangDetailTeamPivotDao> getMilkbangDetailListTeamPivot(@ModelAttribute MilkbangDetailDao inMilkbangDetailDao) {

		return promoService.getMilkbangDetailListTeamPivot(inMilkbangDetailDao);
	}

	@GetMapping("/getMilkbangDetail")
	public List<MilkbangDetailDao> getMilkbangDetail(@ModelAttribute MilkbangDetailDao inMilkbangDetailDao) {

		return promoService.getMilkbangDetail(inMilkbangDetailDao);
	}

	@GetMapping("/getAllPromoTeam")
	public List<PromoTeamDao> getAllPromoTeam(@ModelAttribute PromoTeamDao inPromoTeamDao) {

		return promoService.getAllPromoTeam(inPromoTeamDao);
	}
	
	@GetMapping("/getValidPromoTeam")
	public List<PromoTeamDao> getValidPromoTeam(@ModelAttribute PromoTeamDao inPromoTeamDao) {

		return promoService.getValidPromoTeam(inPromoTeamDao);
	}

	/**
	 * 판촉실적 마감 처리
	 * 
	 * @param dataList 마감 처리할 담당자 목록 (담당자코드, 기간, 마감사유 필수)
	 * @return 마감 처리 결과 (성공 여부, 메시지, 마감 건수)
	 */
	@PostMapping("/closePromo")
	public ResponseEntity<Map<String, Object>> closePromo(@RequestBody List<PromoCloseDao> dataList) {

		try {
			System.out.println("=== 판촉실적 마감 시작 ===");
			System.out.println("마감 요청 담당자 수: " + (dataList != null ? dataList.size() : 0));

			// ✅ 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("마감 처리할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "마감 처리할 데이터가 없습니다.");
				errorResponse.put("closedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// ✅ 필수 값 체크
			for (PromoCloseDao data : dataList) {
				if (data.getTeamPersonCd() == null || data.getTeamPersonCd().isEmpty()) {
					System.out.println("필수 값(담당자코드)이 누락되었습니다.");

					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("success", false);
					errorResponse.put("message", "필수 값(담당자코드)이 누락되었습니다.");
					errorResponse.put("closedCount", 0);

					return ResponseEntity.badRequest().body(errorResponse);
				}

				if (data.getStartDate() == null || data.getStartDate().isEmpty() || data.getEndDate() == null
						|| data.getEndDate().isEmpty()) {
					System.out.println("필수 값(기간)이 누락되었습니다.");

					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("success", false);
					errorResponse.put("message", "필수 값(기간)이 누락되었습니다.");
					errorResponse.put("closedCount", 0);

					return ResponseEntity.badRequest().body(errorResponse);
				}
			}

			// ✅ 서비스 호출
			int closedCount = promoService.closePromo(dataList);

			System.out.println("마감 완료 - 성공: " + closedCount + "건");
			System.out.println("=== 판촉실적 마감 완료 ===");

			// ✅ 성공 응답
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", dataList.size() + "명의 판촉실적이 성공적으로 마감되었습니다. (총 " + closedCount + "건)");
			response.put("closedCount", closedCount);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: " + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("closedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (Exception e) {
			System.out.println("판촉실적 마감 중 오류 발생: " + e.getMessage());
			e.printStackTrace();

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 마감 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("closedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	/**
	 * 판촉실적 마감해제 처리
	 * 
	 * @param dataList 마감해제 처리할 담당자 목록 (담당자코드, 기간 필수)
	 * @return 마감해제 처리 결과 (성공 여부, 메시지, 해제 건수)
	 */
	@PostMapping("/unclosePromo")
	public ResponseEntity<Map<String, Object>> unclosePromo(@RequestBody List<PromoCloseDao> dataList) {

		try {
			System.out.println("=== 판촉실적 마감해제 시작 ===");
			System.out.println("마감해제 요청 담당자 수: " + (dataList != null ? dataList.size() : 0));

			// ✅ 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("마감해제 처리할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "마감해제 처리할 데이터가 없습니다.");
				errorResponse.put("unclosedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// ✅ 필수 값 체크
			for (PromoCloseDao data : dataList) {
				if (data.getTeamPersonCd() == null || data.getTeamPersonCd().isEmpty()) {
					System.out.println("필수 값(담당자코드)이 누락되었습니다.");

					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("success", false);
					errorResponse.put("message", "필수 값(담당자코드)이 누락되었습니다.");
					errorResponse.put("unclosedCount", 0);

					return ResponseEntity.badRequest().body(errorResponse);
				}

				if (data.getStartDate() == null || data.getStartDate().isEmpty() || data.getEndDate() == null
						|| data.getEndDate().isEmpty()) {
					System.out.println("필수 값(기간)이 누락되었습니다.");

					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("success", false);
					errorResponse.put("message", "필수 값(기간)이 누락되었습니다.");
					errorResponse.put("unclosedCount", 0);

					return ResponseEntity.badRequest().body(errorResponse);
				}
			}

			// ✅ 서비스 호출
			int unclosedCount = promoService.unclosePromo(dataList);

			System.out.println("마감해제 완료 - 성공: " + unclosedCount + "건");
			System.out.println("=== 판촉실적 마감해제 완료 ===");

			// ✅ 성공 응답
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", dataList.size() + "명의 판촉실적 마감이 성공적으로 해제되었습니다. (총 " + unclosedCount + "건)");
			response.put("unclosedCount", unclosedCount);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: " + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("unclosedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (Exception e) {
			System.out.println("판촉실적 마감해제 중 오류 발생: " + e.getMessage());
			e.printStackTrace();

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 마감해제 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("unclosedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	@PostMapping("/savePromoDetail")
	public ResponseEntity<Map<String, Object>> savePromoDetail(@RequestBody List<MilkbangDetailDao> dataList) {

		try {
			System.out.println("=== 판촉실적 저장 시작 ===");
			System.out.println("변경된 데이터 개수: {}" + dataList != null ? dataList.size() : 0);

			// 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("저장할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "저장할 데이터가 없습니다.");
				errorResponse.put("savedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// 서비스 호출
			int savedCount = promoService.savePromoDetail(dataList);

			System.out.println("저장 완료 - 성공: {}건" + savedCount);
			System.out.println("=== 판촉실적 저장 완료 ===");

			// ✅ 성공 응답 (Map)
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", savedCount + "건의 데이터가 성공적으로 저장되었습니다.");
			response.put("savedCount", savedCount);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: {}" + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (Exception e) {
			System.out.println("판촉실적 저장 중 오류 발생" + e);

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}
	
	// 해피콜 상세 저장
	@PostMapping("/saveHappyDetail")
	public ResponseEntity<Map<String, Object>> saveHappyDetail(@RequestBody List<MilkbangDetailDao> dataList) {

		try {
			System.out.println("=== 해피콜 저장 시작 ===");
			System.out.println("변경된 데이터 개수: {}" + dataList != null ? dataList.size() : 0);

			// 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("저장할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "저장할 데이터가 없습니다.");
				errorResponse.put("savedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// 서비스 호출
			int savedCount = promoService.saveHappyDetail(dataList);

			System.out.println("저장 완료 - 성공: {}건" + savedCount);
			System.out.println("=== 해피콜 저장 완료 ===");

			// ✅ 성공 응답 (Map)
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", savedCount + "건의 데이터가 성공적으로 저장되었습니다.");
			response.put("savedCount", savedCount);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: {}" + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (Exception e) {
			System.out.println("판촉실적 저장 중 오류 발생" + e);

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	@PostMapping("/savePromo")
	public ResponseEntity<Map<String, Object>> savePromo(@RequestBody List<MilkbangDetailDao> dataList) {

		try {
			System.out.println("=== 판촉실적 저장 시작 ===");
			System.out.println("변경된 데이터 개수: {}" + dataList != null ? dataList.size() : 0);

			// 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("저장할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "저장할 데이터가 없습니다.");
				errorResponse.put("savedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// 서비스 호출
			int savedCount = promoService.savePromo(dataList);

			System.out.println("저장 완료 - 성공: {}건" + savedCount);
			System.out.println("=== 판촉실적 저장 완료 ===");

			// ✅ 성공 응답 (Map)
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", savedCount + "건의 데이터가 성공적으로 저장되었습니다.");
			response.put("savedCount", savedCount);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: {}" + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (Exception e) {
			System.out.println("판촉실적 저장 중 오류 발생" + e);

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}
	
	@PostMapping("/saveHappyCall")
	public ResponseEntity<Map<String, Object>> saveHappyCall(@RequestBody List<MilkbangDetailDao> dataList) {

		try {
			System.out.println("=== 판촉실적 저장 시작 ===");
			System.out.println("변경된 데이터 개수: {}" + dataList != null ? dataList.size() : 0);

			// 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("저장할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "저장할 데이터가 없습니다.");
				errorResponse.put("savedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// 서비스 호출
			int savedCount = promoService.saveHappyCall(dataList);

			System.out.println("저장 완료 - 성공: {}건" + savedCount);
			System.out.println("=== 해피콜 저장 완료 ===");

			// ✅ 성공 응답 (Map)
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", savedCount + "건의 데이터가 성공적으로 저장되었습니다.");
			response.put("savedCount", savedCount);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: {}" + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (Exception e) {
			System.out.println("해피콜 저장 중 오류 발생" + e);

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 저장 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("savedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	/**
	 * 판촉실적 삭제
	 * 
	 * @param dataList 삭제할 데이터 목록 (orderCd, orderSeq 필수)
	 * @return 삭제 결과
	 */
	@PostMapping("/deletePromo")
	public ResponseEntity<Map<String, Object>> deletePromo(@RequestBody List<MilkbangDetailDao> dataList) {

		try {
			System.out.println("=== 판촉실적 삭제 시작 ===");
			System.out.println("삭제 요청 데이터 개수: " + (dataList != null ? dataList.size() : 0));

			// ✅ 데이터 유효성 검증
			if (dataList == null || dataList.isEmpty()) {
				System.out.println("삭제할 데이터가 없습니다.");

				Map<String, Object> errorResponse = new HashMap<>();
				errorResponse.put("success", false);
				errorResponse.put("message", "삭제할 데이터가 없습니다.");
				errorResponse.put("deletedCount", 0);

				return ResponseEntity.badRequest().body(errorResponse);
			}

			// ✅ orderCd, orderSeq 필수 값 체크
			for (MilkbangDetailDao data : dataList) {
				if (data.getOrderCd() == null || data.getOrderCd().isEmpty() || data.getOrderSeq() == null
						|| data.getOrderSeq().isEmpty()) {

					System.out.println("필수 값(orderCd, orderSeq)이 누락되었습니다.");

					Map<String, Object> errorResponse = new HashMap<>();
					errorResponse.put("success", false);
					errorResponse.put("message", "삭제할 데이터의 식별자(orderCd, orderSeq)가 누락되었습니다.");
					errorResponse.put("deletedCount", 0);

					return ResponseEntity.badRequest().body(errorResponse);
				}
			}

			// ✅ 서비스 호출
			int deletedCount = promoService.deletePromo(dataList);

			System.out.println("삭제 완료 - 성공: " + deletedCount + "건");
			System.out.println("=== 판촉실적 삭제 완료 ===");

			// ✅ 성공 응답
			Map<String, Object> response = new HashMap<>();
			response.put("success", true);
			response.put("message", deletedCount + "건의 데이터가 성공적으로 삭제되었습니다.");
			response.put("deletedCount", deletedCount);

			return ResponseEntity.ok(response);

		} catch (IllegalArgumentException e) {
			System.out.println("잘못된 요청 데이터: " + e.getMessage());

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", e.getMessage());
			errorResponse.put("deletedCount", 0);

			return ResponseEntity.badRequest().body(errorResponse);

		} catch (Exception e) {
			System.out.println("판촉실적 삭제 중 오류 발생: " + e.getMessage());
			e.printStackTrace();

			Map<String, Object> errorResponse = new HashMap<>();
			errorResponse.put("success", false);
			errorResponse.put("message", "데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
			errorResponse.put("deletedCount", 0);

			return ResponseEntity.status(500).body(errorResponse);
		}
	}

	@PostMapping("/uploadMilkbangFile")
	public ResponseEntity<?> uploadMilkbangFile(@RequestParam("file") MultipartFile file,
			@RequestParam(value = "agencyCode", required = false) String agencyCd) {

		try {
			// 파일이 비어있는지 확인
			if (file.isEmpty()) {
				return ResponseEntity.badRequest().body("파일이 비어있습니다.");
			}

			// 원본 파일명
			String originalFileName = file.getOriginalFilename();

			// 파일 저장 경로 생성 (년월별 폴더)
			Path uploadPath = Paths.get(FILE_BASE_PATH);

			// 디렉토리가 없으면 생성
			if (!java.nio.file.Files.exists(uploadPath)) {
				java.nio.file.Files.createDirectories(uploadPath);
			}

			// 파일명 중복 방지를 위한 타임스탬프 추가
//	        String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date());
//	        String fileExtension = "";
//	        if (originalFileName != null && originalFileName.contains(".")) {
//	            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
//	        }
			String savedFileName = originalFileName;

			// DB 저장 로직
			promoService.mergeMilkbangFile(originalFileName);

			// 파일 저장
			Path targetLocation = uploadPath.resolve(savedFileName);
			file.transferTo(targetLocation.toFile());

			return ResponseEntity.ok().body("파일 업로드가 완료되었습니다.");

		} catch (IOException e) {
			return ResponseEntity.status(500).body("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
		} catch (Exception e) {
			return ResponseEntity.status(500).body("파일 정보 저장 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	@GetMapping("/downloadFile")
	public ResponseEntity<Resource> downloadFile(@RequestParam String fileName) {

		try {
			// 파일 경로 생성
			Path filePath = Paths.get(FILE_BASE_PATH).resolve(fileName).normalize();
			Resource resource = new UrlResource(filePath.toUri());

			// 파일이 존재하는지 확인
			if (!resource.exists() || !resource.isReadable()) {
				throw new RuntimeException("파일을 찾을 수 없습니다: " + fileName);
			}

			// 파일명 인코딩 (한글 파일명 처리)
			String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");

			// 응답 헤더 설정
			return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + "\"")
					.body(resource);

		} catch (MalformedURLException e) {
			throw new RuntimeException("파일 경로가 올바르지 않습니다: " + fileName, e);
		} catch (IOException e) {
			throw new RuntimeException("파일 다운로드 중 오류가 발생했습니다: " + fileName, e);
		}
	}

}
