package com.jhworld.catcash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CatAndCashApplication {

	public static void main(String[] args) {
		SpringApplication.run(CatAndCashApplication.class, args);
	}

}
