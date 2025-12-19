package com.yonseidairy.promo.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 비밀번호 해시 유틸리티 (SHA-256 + Salt)
 * - 단방향 암호화 (복호화 불가)
 * - Salt를 사용하여 Rainbow Table 공격 방어
 * - 비밀번호 저장 및 검증에 사용
 */
public class PasswordHashUtil {

    // ✅ 고정 Salt (기존 데이터 호환성 유지용)
    private static final String FIXED_SALT = "YonseiDairy2025!Salt";
    
    // 해시 알고리즘
    private static final String ALGORITHM = "SHA-256";

    /**
     * 비밀번호 해시 생성
     * - 동일한 입력값은 항상 동일한 해시값 생성
     * 
     * @param plainPassword 평문 비밀번호
     * @return Base64 인코딩된 해시값
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty()) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            
            // Salt + Password 조합
            String saltedPassword = FIXED_SALT + plainPassword;
            
            // SHA-256 해시 생성
            byte[] hashBytes = digest.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            
            // Base64 인코딩하여 반환
            return Base64.getEncoder().encodeToString(hashBytes);
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("해시 알고리즘을 찾을 수 없습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 비밀번호 검증
     * - 입력된 비밀번호와 저장된 해시값 비교
     * 
     * @param plainPassword 평문 비밀번호 (사용자 입력)
     * @param hashedPassword 저장된 해시값 (DB)
     * @return 일치 여부
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        // 입력값을 해시하여 저장된 해시와 비교
        String inputHash = hash(plainPassword);
        return hashedPassword.equals(inputHash);
    }
    
    /**
     * 해시값인지 확인
     * - SHA-256 Base64 해시는 항상 44자
     * 
     * @param text 확인할 문자열
     * @return 해시값 여부
     */
    public static boolean isHashed(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        // SHA-256 + Base64 = 항상 44자, Base64 문자만 포함
        return text.length() == 44 && text.matches("^[A-Za-z0-9+/=]+$");
    }
}