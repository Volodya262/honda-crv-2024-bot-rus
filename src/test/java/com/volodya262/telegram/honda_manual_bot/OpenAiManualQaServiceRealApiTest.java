package com.volodya262.telegram.honda_manual_bot;

import com.volodya262.telegram.honda_manual_bot.ai.OpenAiAskResult;
import com.volodya262.telegram.honda_manual_bot.ai.OpenAiManualQaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "runRealApiTests", matches = "true")
class OpenAiManualQaServiceRealApiTest extends BaseRealApiTest {

    @Autowired
    private OpenAiManualQaService openAiManualQaService;

    @Test
    void askManualReturnsStructuredResultFromRealApi() {
        OpenAiAskResult result = assertDoesNotThrow(
                () -> openAiManualQaService.askManual("In my Honda CR-V 2024, the parking sensors keep beeping when I drive into my garage. How do I turn them off?")
        );

        assertTrue(result.isSuccess(), () -> result.errorMessageToLog());
        assertFalse(result.text().isBlank());
        assertFalse(result.detectedUserLanguage().isBlank());
    }
}
