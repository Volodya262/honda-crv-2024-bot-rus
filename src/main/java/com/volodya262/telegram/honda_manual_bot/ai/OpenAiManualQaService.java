package com.volodya262.telegram.honda_manual_bot.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volodya262.telegram.honda_manual_bot.config.OpenAiProperties;
import com.volodya262.telegram.honda_manual_bot.domain.DetectedUserLanguage;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class OpenAiManualQaService {

    private static final String DEFAULT_ERROR_TEXT = "I could not find an answer.";

    private final OpenAiProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;

    public OpenAiManualQaService(
            OpenAiProperties properties,
            RestClient.Builder restClientBuilder
    ) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public OpenAiAskResult askManual(String userQuestion) {
        final var request = OpenAiRequestFactory.from(properties.model(), properties.vectorStoreId(), userQuestion);

        final var rawResponse = restClient.post()
                .uri("/responses")
                .body(request)
                .retrieve()
                .body(String.class);

        return toOpenAiAskResult(rawResponse);
    }

    OpenAiAskResult toOpenAiAskResult(String rawResponse) {
        JsonNode response = readResponse(rawResponse);

        if (response == null) {
            return OpenAiAskResult.errorWithHumanReadable(
                    DetectedUserLanguage.UNKNOWN,
                    "OpenAI returned an empty response.",
                    "OpenAI returned an empty response."
            );
        }

        JsonNode output = response.path("output");
        if (!output.isArray()) {
            return OpenAiAskResult.errorWithHumanReadable(
                    DetectedUserLanguage.UNKNOWN,
                    "Could not parse OpenAI response: missing output array.",
                    "Could not parse OpenAI response."
            );
        }

        String structuredText = findStructuredText(output);

        if (structuredText == null) {
            return OpenAiAskResult.errorWithHumanReadable(
                    DetectedUserLanguage.UNKNOWN,
                    "Could not parse OpenAI response: missing structured output text.",
                    DEFAULT_ERROR_TEXT
            );
        }

        return readAskResult(structuredText);
    }

    private JsonNode readResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return null;
        }

        try {
            return objectMapper.readTree(rawResponse);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse OpenAI response.", e);
        }
    }

    private String findStructuredText(JsonNode output) {
        for (JsonNode outputItem : output) {
            String text = findTextInContent(outputItem.path("content"));

            if (text != null) {
                return text;
            }
        }

        return null;
    }

    private String findTextInContent(JsonNode content) {
        if (!content.isArray()) {
            return null;
        }

        for (JsonNode contentItem : content) {
            JsonNode text = contentItem.path("text");

            if (text.isTextual()) {
                return text.asText();
            }
        }

        return null;
    }

    private OpenAiAskResult readAskResult(String rawText) {
        try {
            OpenAiAskResult result = objectMapper.readValue(rawText, OpenAiAskResult.class);
            DetectedUserLanguage detectedUserLanguage = getOrDefault(result.detectedUserLanguage());

            if (!result.isSuccess()) {
                return OpenAiAskResult.errorWithHumanReadable(
                        detectedUserLanguage,
                        getOrDefault(result.errorMessageToLog(), "OpenAI returned an unsuccessful result."),
                        getOrDefault(result.errorMessageHumanReadable(), "I could not find an answer.")
                );
            }

            if (result.text() == null || result.text().isBlank()) {
                return OpenAiAskResult.errorWithHumanReadable(
                        detectedUserLanguage,
                        "OpenAI returned an empty answer text.",
                        DEFAULT_ERROR_TEXT
                );
            }

            return OpenAiAskResult.success(
                    result.text(),
                    detectedUserLanguage,
                    getOrEmptyList(result.sourcesManualPagesIndexesAsPrinted()),
                    getOrEmptyList(result.sourcesManualLinks())
            );
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not parse structured OpenAI response.", e);
        }
    }

    private DetectedUserLanguage getOrDefault(DetectedUserLanguage detectedUserLanguage) {
        if (detectedUserLanguage == null) {
            return DetectedUserLanguage.UNKNOWN;
        }

        return detectedUserLanguage;
    }

    private <T> List<T> getOrEmptyList(List<T> values) {
        if (values == null) {
            return List.of();
        }

        return values;
    }

    private String getOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }
}
