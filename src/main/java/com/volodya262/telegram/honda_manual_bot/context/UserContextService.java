package com.volodya262.telegram.honda_manual_bot.context;

import com.volodya262.telegram.honda_manual_bot.context.UserContext.UserContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserContextService {

    private static final int MAX_USER_MESSAGES_PER_DAY = 50;

    private final UserContextStorage userContextStorage;
    private final Clock clock;

    @Autowired
    public UserContextService(UserContextStorage userContextStorage) {
        this(userContextStorage, Clock.systemUTC());
    }

    UserContextService(UserContextStorage userContextStorage, Clock clock) {
        this.userContextStorage = userContextStorage;
        this.clock = clock;
    }

    public UserContext addUserMessage(
            UserContextKey key,
            UserInfo userInfo,
            String id,
            String text,
            Instant timestamp
    ) {
        return addMessage(key, userInfo, new ChatMessage(id, MessageRole.USER, text, timestamp));
    }

    public UserContext addAssistantMessage(UserContextKey key, UserInfo userInfo, String text) {
        return addMessage(
                key,
                userInfo,
                new ChatMessage(UUID.randomUUID().toString(), MessageRole.ASSISTANT, text, Instant.now(clock))
        );
    }

    public UserContext addMessage(UserContextKey key, UserInfo userInfo, ChatMessage message) {
        return userContextStorage.appendMessage(key, userInfo, message);
    }

    public boolean checkRateLimits(UserContextKey key) {
        Instant cutoff = Instant.now(clock).minusSeconds(24 * 60 * 60);

        return findByKey(key)
                .map(context -> context.messages().stream()
                        .filter(ChatMessage::isUserMessage)
                        .filter(message -> !message.timestamp().isBefore(cutoff))
                        .count() <= MAX_USER_MESSAGES_PER_DAY)
                .orElse(true);
    }

    public Optional<UserContext> findByKey(UserContextKey key) {
        return userContextStorage.findByKey(key);
    }
}
