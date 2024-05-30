package com.sunbird.serve.fulfill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FulfillApplication {

	public static void main(String[] args) {
		SpringApplication.run(FulfillApplication.class, args);
	}

}
