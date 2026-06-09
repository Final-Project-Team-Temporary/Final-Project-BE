package com.example.whiplash;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class WhiplashApplicationTests {

	@BeforeAll
	static void setUpBeforeClass() throws Exception {
		// .env.test가 없으면 .env로 폴백
		Dotenv env = Dotenv.configure()
				.filename(".env.test")
				.ignoreIfMissing()
				.load();
		if (env.entries().isEmpty()) {
			env = Dotenv.configure().ignoreIfMissing().load();
		}
		env.entries().forEach(e -> System.setProperty(e.getKey(), e.getValue()));
	}
	@Test
	void contextLoads() {
	}

}
