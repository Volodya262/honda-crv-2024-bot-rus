package com.volodya262.telegram.honda_manual_bot;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.telegram.telegrambots.meta.TelegramUrl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

//@Import(TestcontainersConfiguration.class)
@Import(HondaManualBotApplicationTests.TelegramApiMockConfiguration.class)
@SpringBootTest(properties = {
		"telegram.bot.token=test-token",
		"openai.api-key=test-openai-api-key",
		"openai.vector-store-id=test-vector-store-id"
})
class HondaManualBotApplicationTests {

	private static final WireMockServer telegramApi = new WireMockServer(wireMockConfig().dynamicPort());

	static {
		telegramApi.start();
		stubTelegramApi();
	}

	@AfterAll
	static void stopTelegramApi() {
		telegramApi.stop();
	}

	@Test
	void contextLoads() {
	}

	private static void stubTelegramApi() {
		telegramApi.stubFor(post(urlEqualTo("/bottest-token/deleteWebhook"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("""
								{"ok":true,"result":true}
								""")));

		telegramApi.stubFor(post(urlEqualTo("/bottest-token/getUpdates"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("""
								{"ok":true,"result":[]}
								""")));
	}

	@TestConfiguration
	static class TelegramApiMockConfiguration {

		@Bean
		TelegramUrl telegramUrl() {
			return new TelegramUrl("http", "localhost", telegramApi.port(), false);
		}
	}
}
