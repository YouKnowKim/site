package com.yonseidairy.promo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yonseidairy.promo.dao.AgencyDao;
import com.yonseidairy.promo.dao.MilkbangFileDao;
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
	
	@GetMapping("/getMilkbangFileList")
	public List<MilkbangFileDao> getMilkbangFileList(@ModelAttribute MilkbangFileDao inMilkbangFileDao){
		
		return promoService.getMilkbangFileList(inMilkbangFileDao);
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
