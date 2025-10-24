package com.yonseidairy.promo.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
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
	
	@GetMapping("/getAllAgency")
	public List<AgencyDao> getAllAgency(@ModelAttribute AgencyDao inAgencyDao) {
		
		return promoService.getAllAgency(inAgencyDao);
	}
	
	@GetMapping("/getMyAgencyList")
	public List<AgencyDao> getMyAgencyList(@ModelAttribute AgencyDao inAgencyDao) {
		
		return promoService.getMyAgencyList(inAgencyDao);
	}
	
	@GetMapping("/getMilkbangFileList")
	public List<MilkbangFileDao> getMilkbangFileList(@ModelAttribute MilkbangFileDao inMilkbangFileDao){
		
		return promoService.getMilkbangFileList(inMilkbangFileDao);
	}
	
	@GetMapping("/getAllTeamPerson")
	public List<TeamPersonDao> getAllTeamPerson() {
		
		return promoService.getAllTeamPerson();
	}
	
	@GetMapping("/getMilkNotSubmitFileList")
	public List<MilkbangFileDao> getMilkNotSubmitFileList(@ModelAttribute MilkbangFileDao inMilkbangFileDao){
		
		return promoService.getMilkNotSubmitFileList(inMilkbangFileDao);
	}
	
	@PostMapping("/uploadMilkbangFile")
	public ResponseEntity<?> uploadMilkbangFile(
	        @RequestParam("file") MultipartFile file,
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
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");

            // 응답 헤더 설정
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename=\"" + encodedFileName + "\"")
                    .body(resource);

        } catch (MalformedURLException e) {
            throw new RuntimeException("파일 경로가 올바르지 않습니다: " + fileName, e);
        } catch (IOException e) {
            throw new RuntimeException("파일 다운로드 중 오류가 발생했습니다: " + fileName, e);
        }
    }

}
