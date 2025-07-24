package com.yonseidairy.site.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
	
	@GetMapping("/")
	public String index() {
        // return "/index.html"; ← 이건 static 디렉토리일 때
		return "forward:/index.html";
    }

}
