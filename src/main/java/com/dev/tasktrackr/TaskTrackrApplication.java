package com.dev.tasktrackr;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class TaskTrackrApplication {

	public static void main(String[] args) {
		SpringApplication.run(TaskTrackrApplication.class, args);
	}

}
