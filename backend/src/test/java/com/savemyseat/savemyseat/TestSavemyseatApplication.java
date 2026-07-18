package com.savemyseat.savemyseat;

import org.springframework.boot.SpringApplication;

public class TestSavemyseatApplication {

	public static void main(String[] args) {
		SpringApplication.from(SavemyseatApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
