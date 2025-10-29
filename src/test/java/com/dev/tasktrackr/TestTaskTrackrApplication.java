package com.dev.tasktrackr;

import com.dev.tasktrackr.ProjectTests.service.shared.TestContainersConfiguration;
import org.springframework.boot.SpringApplication;

public class TestTaskTrackrApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskTrackrApplication::main).with(TestContainersConfiguration.class).run(args);
	}

}
