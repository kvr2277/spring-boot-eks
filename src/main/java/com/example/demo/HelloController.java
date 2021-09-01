package com.example.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

	@GetMapping("/")
	public String index() {
		return "Hello World!";
	}

    @GetMapping("/name")
	public String getName() {
		return "Vinod here !!";
	}

}