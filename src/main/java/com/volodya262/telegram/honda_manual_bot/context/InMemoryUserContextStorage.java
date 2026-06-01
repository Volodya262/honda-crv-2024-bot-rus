package com.volodya262.telegram.honda_manual_bot.context;

import org.springframework.stereotype.Repository;

import com.volodya262.telegram.honda_manual_bot.context.UserContext.UserContextKey;

import java.time.Clock;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Repository
public class InMemoryUserContextStorage implements UserContextStorage {

    private final ConcurrentMap<UserContextKey, UserContext> storage = new ConcurrentHashMap<>();
    private final Clock clock;

    public InMemoryUserContextStorage() {
        this(Clock.systemUTC());
    }

    InMemoryUserContextStorage(Clock clock) {
        this.clock = clock;
    }

    @Override
    public UserContext appendMessage(UserContextKey key, UserInfo userInfo, ChatMessage message) {
        removeExpiredContexts();

        return storage.compute(key, (__, existingContext) -> {
            if (existingContext == null) {
                return new UserContext(key, userInfo, List.of(message));
            }

            List<ChatMessage> messages = new ArrayList<>(existingContext.messages());
            messages.add(message);
            return new UserContext(key, userInfo, messages);
        });
    }

    @Override
    public Optional<UserContext> findByKey(UserContextKey key) {
        return Optional.ofNullable(storage.get(key));
    }

    private void removeExpiredContexts() {
        final var cutoffInstant = clock.instant().minus(Duration.ofDays(30));

        storage.entrySet().removeIf(entry ->
                entry.getValue().lastUserMessage()
                        .map(message -> message.timestamp().isBefore(cutoffInstant))
                        .orElse(true)
        );
    }
}
