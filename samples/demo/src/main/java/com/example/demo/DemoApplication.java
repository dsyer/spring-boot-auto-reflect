package com.example.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.reflect.SpringApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
@RestController
public class DemoApplication {

	@GetMapping
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(DemoApplication.class, args);
	}

}
