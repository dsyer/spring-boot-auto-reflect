package com.example.auto;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.boot.reflect.AutoTestContextLoader;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = AutoApplication.class, loader = AutoTestContextLoader.class)
public class AutoApplicationTests {

	@Test
	public void contextLoads() {
	}

}
