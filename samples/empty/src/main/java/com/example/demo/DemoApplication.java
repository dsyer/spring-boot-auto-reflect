package com.example.demo;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
public class DemoApplication {

	public static void main(String[] args) throws Exception {
		new SpringApplicationBuilder(DemoApplication.class)
				.properties("--server.port=0", "--spring.jmx.enabled=false").run();
	}

}
