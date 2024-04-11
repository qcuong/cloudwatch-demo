package com.quoccuong.aws.cloudwatch;

import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class CloudwatchDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(CloudwatchDemoApplication.class, args);
	}

}
