package com.volodya262.telegram.honda_manual_bot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAiManualQaServiceRealApiTest extends BaseRealApiTest {

    @Autowired
    private OpenAiManualQaService openAiManualQaService;

    @Test
    void askManualReturnsStructuredResultFromRealApi() {
        OpenAiAskResult result = assertDoesNotThrow(
                () -> openAiManualQaService.askManual("The parking sensors keep beeping when I drive into my garage. How do I turn them off?")
        );

        assertTrue(result.isSuccess(), () -> result.errorMessageToLog());
        assertFalse(result.text().isBlank());
        assertFalse(result.detectedUserLanguage().isBlank());
    }
}
