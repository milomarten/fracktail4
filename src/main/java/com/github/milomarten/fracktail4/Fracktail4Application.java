package com.github.milomarten.fracktail4;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Fracktail4Application {

	public static void main(String[] args) {
		SpringApplication.run(Fracktail4Application.class, args);
	}

	@PostConstruct
	public void test() {
		var pkg = this.getClass().getPackage();
		System.out.println("My version is " + (pkg == null ? "?" : pkg.getImplementationVersion()));
	}
}
