package com.example.bug_tracker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test") // ci.ymlの"SPRING_PROFILES_ACTIVE=test"用
@SpringBootTest
class BugTrackerApplicationTests {

	@Test
	void contextLoads() {
	}

}
