package com.rql.toy.example;

import com.rql.core.config.UseRQL;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@UseRQL
@SpringBootApplication
public class ToyExampleApplication {

	public static void main(String[] args) {
		SpringApplication.run(ToyExampleApplication.class, args);
	}

}
