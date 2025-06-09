package com.jhworld.catcash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
		scanBasePackages = "com.jhworld.catcash",
		exclude = {org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class}
)
public class CatAndCashApplication {
	public static void main(String[] args) {
		SpringApplication.run(CatAndCashApplication.class, args);
	}
}
