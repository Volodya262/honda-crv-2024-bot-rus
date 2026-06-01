package com.volodya262.telegram.honda_manual_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public record TelegramBotProperties(
        String username,
        String token
) {
    public TelegramBotProperties {
        requireText(username, "telegram.bot.username must not be blank");
        requireText(token, "telegram.bot.token must not be blank");
    }

    private static void requireText(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }
}
