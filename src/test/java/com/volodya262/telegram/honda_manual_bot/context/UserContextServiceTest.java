package com.volodya262.telegram.honda_manual_bot.context;

import com.volodya262.telegram.honda_manual_bot.context.UserContext.UserContextKey;
import com.volodya262.telegram.honda_manual_bot.domain.DetectedUserLanguage;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserContextServiceTest {

    @Test
    void addUserMessageAndAddAssistantMessageCreateContextAndAppendMessages() {
        UserContextService userContextService = new UserContextService(
                new InMemoryUserContextStorage(),
                Clock.fixed(Instant.parse("2026-06-01T10:00:01Z"), ZoneOffset.UTC)
        );
        UserContextKey key = new UserContextKey(123L, 456L);
        UserInfo userInfo = new UserInfo(456L, "username", "First", "Last", DetectedUserLanguage.ENGLISH);

        userContextService.addUserMessage(
                key,
                userInfo,
                "message-1",
                "Question",
                Instant.parse("2026-06-01T10:00:00Z")
        );
        UserContext context = userContextService.addAssistantMessage(
                key,
                userInfo,
                "Answer"
        );

        assertEquals(key, context.key());
        assertEquals(userInfo, context.userInfo());
        assertEquals(2, context.messages().size());
        assertEquals(MessageRole.USER, context.messages().getFirst().role());
        assertEquals(MessageRole.ASSISTANT, context.messages().getLast().role());
        assertEquals(Instant.parse("2026-06-01T10:00:01Z"), context.messages().getLast().timestamp());
        assertTrue(userContextService.findByKey(key).isPresent());
    }

    @Test
    void addMessageRemovesContextsWhereUserLastWroteMoreThanMonthAgo() {
        UserContextStorage storage = new InMemoryUserContextStorage(Clock.fixed(
                Instant.parse("2026-06-01T10:00:00Z"),
                ZoneOffset.UTC
        ));
        UserContextService service = new UserContextService(storage);
        UserInfo userInfo = new UserInfo(456L, "username", "First", "Last", DetectedUserLanguage.ENGLISH);
        UserContextKey expiredKey = new UserContextKey(123L, 456L);
        UserContextKey freshKey = new UserContextKey(789L, 456L);

        service.addMessage(
                expiredKey,
                userInfo,
                new ChatMessage("old-user", MessageRole.USER, "Old question", Instant.parse("2026-04-30T09:59:59Z"))
        );
        service.addMessage(
                expiredKey,
                userInfo,
                new ChatMessage("old-assistant", MessageRole.ASSISTANT, "Old answer", Instant.parse("2026-04-30T10:00:00Z"))
        );

        service.addMessage(
                freshKey,
                userInfo,
                new ChatMessage("fresh-user", MessageRole.USER, "Fresh question", Instant.parse("2026-06-01T10:00:00Z"))
        );

        assertFalse(service.findByKey(expiredKey).isPresent());
        assertTrue(service.findByKey(freshKey).isPresent());
    }

    @Test
    void checkRateLimitsReturnsFalseWhenUserSentMoreThanFiftyMessagesDuringLastDay() {
        UserContextService service = new UserContextService(
                new InMemoryUserContextStorage(),
                Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneOffset.UTC)
        );
        UserContextKey key = new UserContextKey(123L, 456L);
        UserInfo userInfo = new UserInfo(456L, "username", "First", "Last", DetectedUserLanguage.ENGLISH);

        for (int i = 1; i <= 50; i++) {
            service.addUserMessage(
                    key,
                    userInfo,
                    "message-%s".formatted(i),
                    "Question",
                    Instant.parse("2026-06-01T09:00:00Z")
            );
        }

        assertTrue(service.checkRateLimits(key));

        service.addUserMessage(
                key,
                userInfo,
                "message-51",
                "Question",
                Instant.parse("2026-06-01T09:00:00Z")
        );

        assertFalse(service.checkRateLimits(key));
    }

    @Test
    void checkRateLimitsIgnoresAssistantMessagesAndUserMessagesOlderThanLastDay() {
        UserContextService service = new UserContextService(
                new InMemoryUserContextStorage(),
                Clock.fixed(Instant.parse("2026-06-01T10:00:00Z"), ZoneOffset.UTC)
        );
        UserContextKey key = new UserContextKey(123L, 456L);
        UserInfo userInfo = new UserInfo(456L, "username", "First", "Last", DetectedUserLanguage.ENGLISH);

        service.addUserMessage(
                key,
                userInfo,
                "old-message",
                "Old question",
                Instant.parse("2026-05-31T09:59:59Z")
        );

        for (int i = 1; i <= 100; i++) {
            service.addAssistantMessage(key, userInfo, "Answer");
        }

        assertTrue(service.checkRateLimits(key));
    }
}
