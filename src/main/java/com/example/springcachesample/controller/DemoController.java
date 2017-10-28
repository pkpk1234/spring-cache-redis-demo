package com.example.springcachesample.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class DemoController {

	@GetMapping("/test")
	@Cacheable("personCache")
	public String test() {
		return "test";
	}

	@GetMapping("/test/{id}")
	@Cacheable("personCache")
	public String test(@PathVariable("id") String id) {
		return "test" + id;
	}

	@GetMapping("/test3")
	@Cacheable("demo")
	public String test3() {
		log.info("cache not hit,load from method test3");
		return "test3";
	}
}
