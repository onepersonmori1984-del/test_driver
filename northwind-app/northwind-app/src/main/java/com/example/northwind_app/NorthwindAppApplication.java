package com.example.northwind_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NorthwindAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(NorthwindAppApplication.class, args);
	}

}
