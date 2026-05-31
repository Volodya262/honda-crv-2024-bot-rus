package com.volodya262.telegram.honda_manual_bot;

import org.springframework.boot.SpringApplication;

public class TestHondaManualBotApplication {

	public static void main(String[] args) {
		SpringApplication.from(HondaManualBotApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
