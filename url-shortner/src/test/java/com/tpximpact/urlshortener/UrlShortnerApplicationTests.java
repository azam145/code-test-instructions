package com.tpximpact.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class) // Add this line!
@SpringBootTest
class UrlShortnerApplicationTests {

	@Test
	void contextLoads() {
		// This test will now wait for the Postgres container to start
	}
}