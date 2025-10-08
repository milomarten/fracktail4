package com.github.milomarten.fracktail.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan({"com.github.milomarten.fracktail.core", "com.github.milomarten.${app.version}"})
public class FracktailApplication {

	public static void main(String[] args) {
		SpringApplication.run(FracktailApplication.class, args);
	}
}
