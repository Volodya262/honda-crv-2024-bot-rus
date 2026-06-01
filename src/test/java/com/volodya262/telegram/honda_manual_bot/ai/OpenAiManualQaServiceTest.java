package com.volodya262.telegram.honda_manual_bot.ai;

import com.volodya262.telegram.honda_manual_bot.config.OpenAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiManualQaServiceTest {

    private final OpenAiManualQaService service = new OpenAiManualQaService(
            new OpenAiProperties("test-api-key", "test-model", "test-vector-store-id"),
            RestClient.builder()
    );

    @Test
    void extractResultReturnsSendableErrorWhenResponseIsEmpty() {
        OpenAiAskResult result = service.extractResult(null);

        assertFalse(result.isSuccess());
        assertEquals(DetectedUserLanguage.UNKNOWN, result.detectedUserLanguage());
        assertNull(result.text());
        assertEquals("OpenAI returned an empty response.", result.errorMessageToLog());
        assertEquals("OpenAI returned an empty response.", result.errorMessageHumanReadable());
    }

    @Test
    void extractResultReturnsSendableErrorWhenStructuredTextIsMissing() {
        OpenAiAskResult result = service.extractResult("""
                {
                  "output": [
                    {
                      "content": []
                    }
                  ]
                }
                """);

        assertFalse(result.isSuccess());
        assertNull(result.text());
        assertEquals("Could not parse OpenAI response: missing structured output text.", result.errorMessageToLog());
        assertEquals("I could not find an answer.", result.errorMessageHumanReadable());
        assertTrue(result.sourcesManualPagesIndexesAsPrinted().isEmpty());
        assertTrue(result.sourcesManualLinks().isEmpty());
    }
}
