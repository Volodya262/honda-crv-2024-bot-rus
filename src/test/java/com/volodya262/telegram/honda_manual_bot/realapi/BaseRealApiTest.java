package com.volodya262.telegram.honda_manual_bot.realapi;

import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@EnabledIfSystemProperty(named = "runRealApiTests", matches = "true")
@SpringBootTest
abstract class BaseRealApiTest {

    private static final Map<String, String> DOT_ENV = loadDotEnv();

    @DynamicPropertySource
    static void registerRealApiProperties(DynamicPropertyRegistry registry) {
        registry.add("telegram.bot.token", () -> requiredDotEnv("TELEGRAM_BOT_TOKEN"));
        registry.add("openai.api-key", () -> requiredDotEnv("OPENAI_API_KEY"));
        registry.add("openai.vector-store-id", () -> requiredDotEnv("OPENAI_VECTOR_STORE_ID"));
    }

    private static String requiredDotEnv(String key) {
        String value = DOT_ENV.get(key);

        if (value == null || value.isBlank()) {
            throw new IllegalStateException(key + " must be present in .env for real API tests.");
        }

        return value;
    }

    private static Map<String, String> loadDotEnv() {
        Path dotEnv = Path.of(".env");

        if (!Files.exists(dotEnv)) {
            return Map.of();
        }

        Map<String, String> result = new HashMap<>();

        try {
            for (String line : Files.readAllLines(dotEnv)) {
                int separator = line.indexOf('=');

                if (line.isBlank() || line.stripLeading().startsWith("#") || separator < 1) {
                    continue;
                }

                String key = line.substring(0, separator).trim();
                String value = line.substring(separator + 1).trim();

                result.put(key, unquote(value));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not read .env for real API tests.", e);
        }

        return result;
    }

    private static String unquote(String value) {
        if (value.length() >= 2
                && ((value.startsWith("\"") && value.endsWith("\""))
                || (value.startsWith("'") && value.endsWith("'")))) {
            return value.substring(1, value.length() - 1);
        }

        return value;
    }
}
