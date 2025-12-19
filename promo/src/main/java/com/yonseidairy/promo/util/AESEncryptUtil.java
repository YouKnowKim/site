package com.yonseidairy.promo.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 양방향 암호화 유틸리티 (AES-256)
 * - 암호화/복호화 모두 가능
 * - 외부 시스템 연동, 임시 비밀번호 전송 등에 사용
 */
public class AESEncryptUtil {

    private static final String SECRET_KEY = "YonseiDairy2025!SecretKey1234567";  // 32자
    
    // 16바이트 초기화 벡터
    private static final String IV = "YonseiDairy16IV!";  // 16자

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * AES-256 암호화
     * @param plainText 평문
     * @return Base64 인코딩된 암호문
     */
    public static String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                return null;
            }

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().withoutPadding().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("암호화 실패: " + e.getMessage(), e);
        }
    }

    /**
     * AES-256 복호화
     * @param encryptedText Base64 인코딩된 암호문
     * @return 평문
     */
    public static String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                return null;
            }

            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(IV.getBytes(StandardCharsets.UTF_8));

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decoded = Base64.getDecoder().decode(encryptedText);
            byte[] decrypted = cipher.doFinal(decoded);
            return new String(decrypted, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("복호화 실패: " + e.getMessage(), e);
        }
    }
}