package com.example.demo;

import org.springframework.boot.SpringApplication;

public class TestTaskTrackrApplication {

	public static void main(String[] args) {
		SpringApplication.from(TaskTrackrApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
