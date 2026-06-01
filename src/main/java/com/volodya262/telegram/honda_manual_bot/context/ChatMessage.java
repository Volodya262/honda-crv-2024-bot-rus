package com.volodya262.telegram.honda_manual_bot.context;

import java.time.Instant;

public record ChatMessage(
        String id,
        MessageRole role,
        String text,
        Instant timestamp
) {
    public boolean isUserMessage() {
        return role == MessageRole.USER;
    }
}
