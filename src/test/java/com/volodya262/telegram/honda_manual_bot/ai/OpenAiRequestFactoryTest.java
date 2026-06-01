package com.volodya262.telegram.honda_manual_bot.ai;

import com.volodya262.telegram.honda_manual_bot.context.ChatMessage;
import com.volodya262.telegram.honda_manual_bot.context.MessageRole;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OpenAiRequestFactoryTest {

    @Test
    void fromAddsChatMessageHistoryToInput() {
        Map<String, Object> request = OpenAiRequestFactory.from(
                "test-model",
                "test-vector-store-id",
                List.of(
                        new ChatMessage("message-1", MessageRole.USER, "First question", Instant.parse("2026-06-01T10:00:00Z")),
                        new ChatMessage("message-2", MessageRole.ASSISTANT, "First answer", Instant.parse("2026-06-01T10:00:01Z")),
                        new ChatMessage("message-3", MessageRole.USER, "Follow-up question", Instant.parse("2026-06-01T10:00:02Z"))
                )
        );

        @SuppressWarnings("unchecked")
        List<Map<String, String>> input = (List<Map<String, String>>) request.get("input");

        assertEquals("system", input.getFirst().get("role"));
        assertEquals("user", input.get(1).get("role"));
        assertEquals("First question", input.get(1).get("content"));
        assertEquals("assistant", input.get(2).get("role"));
        assertEquals("First answer", input.get(2).get("content"));
        assertEquals("user", input.get(3).get("role"));
        assertEquals("Follow-up question", input.get(3).get("content"));
    }

    @Test
    void fromAddsResponseSettings() {
        Map<String, Object> request = OpenAiRequestFactory.from(
                "test-model",
                "test-vector-store-id",
                List.of(new ChatMessage("message-1", MessageRole.USER, "Question", Instant.parse("2026-06-01T10:00:00Z")))
        );

        assertEquals(0, request.get("temperature"));
        assertEquals(1200, request.get("max_output_tokens"));
        assertEquals(2, request.get("max_tool_calls"));
        assertEquals("auto", request.get("tool_choice"));
        assertEquals(false, request.get("parallel_tool_calls"));
        assertEquals("auto", request.get("truncation"));
        assertEquals(false, request.get("store"));
    }
}
