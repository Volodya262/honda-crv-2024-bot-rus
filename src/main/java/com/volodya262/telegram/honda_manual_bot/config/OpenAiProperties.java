package com.volodya262.telegram.honda_manual_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "openai")
public record OpenAiProperties(
        String apiKey,
        String model,
        String vectorStoreId
) {
    public OpenAiProperties {
        requireText(apiKey, "openai.api-key must not be blank");
        requireText(model, "openai.model must not be blank");
        requireText(vectorStoreId, "openai.vector-store-id must not be blank");
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
