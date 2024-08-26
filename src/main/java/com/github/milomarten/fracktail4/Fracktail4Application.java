package com.github.milomarten.fracktail4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Fracktail4Application {

	public static void main(String[] args) {
		SpringApplication.run(Fracktail4Application.class, args);
	}

}
