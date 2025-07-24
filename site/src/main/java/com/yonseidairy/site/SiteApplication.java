package com.yonseidairy.site;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class SiteApplication extends SpringBootServletInitializer {
	
	//(war 배포를 위한 소스)
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(SiteApplication.class);
	}

	public static void main(String[] args) {
		SpringApplication.run(SiteApplication.class, args);
	}

}
