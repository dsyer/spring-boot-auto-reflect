package com.example.demo;

import java.io.Closeable;
import java.io.IOException;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
// @Import(LazyInitBeanFactoryPostProcessor.class)
@RestController
public class DemoApplication implements Runnable, Closeable {

	private ConfigurableApplicationContext context;

	@GetMapping
	public String home() {
		return "Hello";
	}

	public static void main(String[] args) throws Exception {
		DemoApplication last = new DemoApplication();
		last.run();
		if (Boolean.getBoolean("demo.close")) {
			last.close();
		}
	}

	@Override
	public void close() throws IOException {
		if (context != null) {
			context.close();
		}
	}

	@Override
	public void run() {
		context = new SpringApplicationBuilder(DemoApplication.class)
				.properties("--server.port=0", "--spring.jmx.enabled=false").run();
	}

}
