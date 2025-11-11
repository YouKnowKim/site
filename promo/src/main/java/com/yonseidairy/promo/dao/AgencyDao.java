package com.yonseidairy.promo.dao;

import lombok.Data;

@Data
public class AgencyDao {
	
	private String salesMan;
	
	private String empyNme;

	private String managerYn;
	
	private String changeType;
	
	private String no;
	
	private String deleteNm;
	
	/** 대리점 코드 */
    private String agencyCd;
    
    /** 대리점명 */
    private String agencyNm;
    
    /** 고객번호 (CD_CUSTOMER 테이블) */
    private String custno;
    
    /** 고객명 (CD_CUSTOMER 테이블) */
    private String custName;
    
    /** 팀 담당자 코드 */
    private String teamPersonCd;
    
    /** 팀 담당자명 */
    private String teamPersonNm;
    
    /** 대리점 전화번호 */
    private String agencyTel;
    
    /** 대리점 휴대폰번호 */
    private String agencyCellPhone;
    
    /** 대리점 주소 */
    private String agencyAddress;
    
    /** 대리점 번호 */
    private String agencyNo;
    
    /** 은행 사용자명 */
    private String bankuserNm;
    
    /** 은행명 */
    private String bankNm;
    
    /** 계좌번호 */
    private String bankNo;
    
    /** 가상계좌 은행명 */
    private String virtualBankNm;
    
    /** 가상계좌번호(일반) */
    private String virtualBankNoHome;
    
    /** 가상계좌번호(특판) */
    private String virtualBankNoSpecial;
    
    /** 삭제여부 (1: 삭제, 0: 미삭제) */
    private String deleteYn;
}
