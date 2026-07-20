package com.aiprep.interview;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync      // used for sending emails / calling AI without blocking the request thread where relevant
@EnableScheduling // used for daily free-quota resets
public class InterviewPlatformApplication {
	public static void main(String[] args) {
		SpringApplication.run(InterviewPlatformApplication.class, args);
	}
}
