package com.yonseidairy.promo.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 비정상적인 HTTP 요청을 필터링하는 Filter
 * - 잘못된 바이트 시퀀스가 포함된 요청 차단
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MalformedRequestFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(MalformedRequestFilter.class);
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        try {
            // 요청 URI 검증
            String uri = httpRequest.getRequestURI();
            if (uri != null && containsInvalidCharacters(uri)) {
                logger.warn("잘못된 문자가 포함된 요청 차단: {}", 
                           httpRequest.getRemoteAddr());
                httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            chain.doFilter(request, response);
            
        } catch (Exception e) {
            logger.error("요청 처리 중 오류: {}", e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
    
    /**
     * 비정상적인 제어 문자 포함 여부 검사
     */
    private boolean containsInvalidCharacters(String value) {
        for (char c : value.toCharArray()) {
            // 제어 문자(0x00-0x1F) 검사 (탭, 줄바꿈 제외)
            if (c < 0x20 && c != 0x09 && c != 0x0A && c != 0x0D) {
                return true;
            }
        }
        return false;
    }
}
