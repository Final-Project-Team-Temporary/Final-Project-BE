package com.example.whiplash;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WhiplashApplicationTests {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		Dotenv env = Dotenv
				.configure()
				.filename(".env.test")
				.load();
		env.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
	}
	@Test
	void contextLoads() {
	}

}
