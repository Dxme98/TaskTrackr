package com.dev.tasktrackr;

import com.dev.tasktrackr.ProjectTests.service.shared.TestContainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestContainersConfiguration.class)
@SpringBootTest
class TaskTrackrApplicationTests {

	@Test
	void contextLoads() {
	}

}
