package com.volodya262.telegram.honda_manual_bot.realapi;

import com.volodya262.telegram.honda_manual_bot.domain.DetectedUserLanguage;
import com.volodya262.telegram.honda_manual_bot.ai.OpenAiAskResult;
import com.volodya262.telegram.honda_manual_bot.ai.OpenAiManualQaService;
import com.volodya262.telegram.honda_manual_bot.context.ChatMessage;
import com.volodya262.telegram.honda_manual_bot.context.MessageRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIfSystemProperty(named = "runRealApiTests", matches = "true")
class OpenAiManualQaServiceRealApiTest extends BaseRealApiTest {

    @Autowired
    private OpenAiManualQaService openAiManualQaService;

    @Test
    void askManualReturnsEnglishParkingSensorAnswerFromRealApi() {
        OpenAiAskResult result = assertDoesNotThrow(
                () -> openAiManualQaService.askManual(userMessage("In my Honda CR-V 2024, the parking sensors keep beeping when I drive into my garage. How do I turn them off?"))
        );

        assertTrue(result.isSuccess());
        assertNull(result.errorMessageToLog());
        assertEquals(DetectedUserLanguage.ENGLISH, result.detectedUserLanguage());
        assertNotBlank(result.text());
        assertFalse(result.sourcesManualPagesIndexesAsPrinted().isEmpty());
        assertTrue(result.sourcesManualPagesIndexesAsPrinted().stream().anyMatch(page -> page.contains("604")));

        String lowerCaseText = result.text().toLowerCase(Locale.ROOT);
        assertTrue(lowerCaseText.contains("parking"));
        assertTrue(lowerCaseText.contains("sensor"));
        assertTrue(lowerCaseText.contains("off"));
    }

    @Test
    void askManualReturnsRussianParkingSensorAnswerFromRealApi() {
        OpenAiAskResult result = assertDoesNotThrow(
                () -> openAiManualQaService.askManual(userMessage("Парктроники постоянно пищат когда я заезжаю в гараж. Как их отключить?"))
        );

        assertTrue(result.isSuccess());
        assertNull(result.errorMessageToLog());
        assertEquals(DetectedUserLanguage.RUSSIAN, result.detectedUserLanguage());
        assertNotBlank(result.text());
        assertFalse(result.sourcesManualPagesIndexesAsPrinted().isEmpty());
    }

    @Test
    void askManualRejectsReactComponentRequestFromRealApi() {
        OpenAiAskResult result = assertDoesNotThrow(
                () -> openAiManualQaService.askManual(userMessage("Напиши пример react компонента"))
        );

        assertFalse(result.isSuccess());
        assertEquals(DetectedUserLanguage.RUSSIAN, result.detectedUserLanguage());
        assertNull(result.text());
        assertNotBlank(result.errorMessageToLog());
        assertNotBlank(result.errorMessageHumanReadable());
        assertTrue(result.sourcesManualPagesIndexesAsPrinted().isEmpty());
    }

    @Test
    void askManualRejectsCookingRequestFromRealApi() {
        OpenAiAskResult result = assertDoesNotThrow(
                () -> openAiManualQaService.askManual(userMessage("Как приготовить блинчики с мясом, сидя в Honda CRV"))
        );

        assertFalse(result.isSuccess());
        assertEquals(DetectedUserLanguage.RUSSIAN, result.detectedUserLanguage());
        assertNull(result.text());
        assertNotBlank(result.errorMessageToLog());
        assertNotBlank(result.errorMessageHumanReadable());
        assertTrue(result.sourcesManualPagesIndexesAsPrinted().isEmpty());
    }

    private void assertNotBlank(String value) {
        assertNotNull(value);
        assertFalse(value.isBlank());
    }

    private List<ChatMessage> userMessage(String text) {
        return List.of(new ChatMessage("test-user-message", MessageRole.USER, text, Instant.parse("2026-06-01T10:00:00Z")));
    }
}
