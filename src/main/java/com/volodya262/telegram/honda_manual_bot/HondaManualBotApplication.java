package com.volodya262.telegram.honda_manual_bot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class HondaManualBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(HondaManualBotApplication.class, args);
	}

}
