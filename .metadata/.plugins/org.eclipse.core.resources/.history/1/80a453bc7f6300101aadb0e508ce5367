package com.yonseidairy.site.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {
	
	@Bean
	public WebServerFactoryCustomizer<TomcatServletWebServerFactory> customizer() {
        return factory -> {
            factory.setPort(8081); // 포트 지정
            factory.setContextPath(""); // context root
            factory.setUriEncoding(java.nio.charset.StandardCharsets.UTF_8);
        };
    }

}
