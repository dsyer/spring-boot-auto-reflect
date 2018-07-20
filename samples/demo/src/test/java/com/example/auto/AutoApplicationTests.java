package com.example.auto;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.reflect.AutoTestContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(classes = AutoApplication.class, loader = AutoTestContextLoader.class)
public class AutoApplicationTests {

	@Autowired
	private WebTestClient rest;

	@Test
	public void contextLoads() {
		rest.get().uri("/").exchange().expectBody(String.class).isEqualTo("Hello");
	}

}
