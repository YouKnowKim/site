package com.milk.batch.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BatchMainController {

	@GetMapping({"/batch/_milkbang_file"})
	@Scheduled(fixedDelay = 3600000L)
	public void _milkbang_file() {
		System.out.println("테스트");
	}
}
