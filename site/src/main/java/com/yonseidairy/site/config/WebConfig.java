package com.yonseidairy.site.config;

import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer, ErrorViewResolver{

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**") // 모든 경로 허용
                .allowedOrigins("*") // 모든 Origin 허용
                .allowedMethods("*") // 모든 HTTP 메서드 허용
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(false); // 쿠키 허용 여부 (필요 시 true로 설정)
			}
		};
	}
	
	@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(0);
    }
	
	@Override
    public ModelAndView resolveErrorView(HttpServletRequest request, 
                                       HttpStatus status, 
                                       Map<String, Object> model) {
        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        
        // API 경로는 제외
        if (path != null && path.startsWith("/api/")) {
            return null; // 기본 에러 페이지 사용
        }
        
        // 나머지는 index.html로
        if (status == HttpStatus.NOT_FOUND) {
            return new ModelAndView("forward:/index.html");
        }
        return null;
    }
}
