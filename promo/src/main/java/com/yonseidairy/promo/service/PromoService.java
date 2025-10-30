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
	
	@Transactional(rollbackFor = Exception.class)
	public Integer savePromo(List<MilkbangDetailDao> dataList) throws Exception {
		
		Integer result = 0;
		
		for(MilkbangDetailDao data : dataList) {
			
			promoMapper.mergePromo(data);
			result++;
		}
		
		return result;
	}
	
	@Transactional(rollbackFor = Exception.class)
	public Integer savePromoDetail(List<MilkbangDetailDao> dataList) throws Exception {
		
		Integer result = 0;
		
		for(MilkbangDetailDao data : dataList) {
			
			promoMapper.mergePromo(data);
			promoMapper.mergePromoDetail(data);
			result++;
		}
		
		return result;
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
        
        if(fileCount <= 0) {
        	promoMapper.insertMilkbangFile(fileDao);
        } else {
        	promoMapper.updateMilkbangFile(fileDao);
        }
		
		return 0;
	}
	
	/**
	 * 파일명에서 대리점 코드 추출 및 변환
	 * 예: 10001_용산_250922_0928.xls -> "001" -> 10001
	 *     12345_강남_250922_0928.xls -> "345" -> 10345
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