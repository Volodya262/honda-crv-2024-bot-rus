package com.volodya262.telegram.honda_manual_bot.context;

import java.util.List;
import java.util.Optional;

public record UserContext(
        UserContextKey key,
        UserInfo userInfo,
        List<ChatMessage> messages
) {
    public UserContext {
        messages = List.copyOf(messages);
    }

    public Optional<ChatMessage> lastUserMessage() {
        for (int i = messages.size() - 1; i >= 0; i--) {
            ChatMessage message = messages.get(i);

            if (message.isUserMessage()) {
                return Optional.of(message);
            }
        }

        return Optional.empty();
    }

    public List<ChatMessage> getLastMessages(int limit) {
        if (limit < 0) {
            throw new IllegalArgumentException("Limit must not be negative.");
        }

        int fromIndex = Math.max(messages.size() - limit, 0);
        return messages.subList(fromIndex, messages.size());
    }

    public record UserContextKey(
            Long chatId,
            Long userId
    ) {
    }
}
