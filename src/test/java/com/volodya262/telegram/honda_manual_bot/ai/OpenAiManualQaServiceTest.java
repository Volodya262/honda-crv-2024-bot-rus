package com.volodya262.telegram.honda_manual_bot.ai;

import com.volodya262.telegram.honda_manual_bot.config.OpenAiProperties;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        assertEquals("Unknown", result.detectedUserLanguage());
        assertNotNull(result.text());
        assertFalse(result.text().isBlank());
        assertEquals("OpenAI returned an empty response.", result.errorMessageToLog());
        assertEquals(result.text(), result.errorMessageHumanReadable());
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
        assertNotNull(result.text());
        assertFalse(result.text().isBlank());
        assertEquals("Could not parse OpenAI response: missing structured output text.", result.errorMessageToLog());
        assertTrue(result.sourcesManualPagesIndexesAsPrinted().isEmpty());
        assertTrue(result.sourcesManualLinks().isEmpty());
    }
}
