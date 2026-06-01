package com.volodya262.telegram.honda_manual_bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.volodya262.telegram.honda_manual_bot.config.OpenAiProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenAiManualQaService {

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
        Map<String, Object> request = Map.of(
                "model", properties.model(),
                "tools", List.of(
                        Map.of(
                                "type", "file_search",
                                "vector_store_ids", List.of(properties.vectorStoreId())
                        ),
                        Map.of("type", "web_search")
                ),
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        Your task is to answer the user's question about the Honda CR-V 2024.
                                        Only answer questions that are directly related to the Honda CR-V 2024, its manual, operation, maintenance, features, warnings, troubleshooting, or ownership.
                                        If the user asks for anything outside that scope, do not answer the unrelated request.
                                        For out-of-scope requests, politely say in the user's language that you can only help with questions about the Honda CR-V 2024, set isSuccess to false, use an empty text if appropriate, provide a human-readable error message, and leave source arrays empty.
                                        Use the Honda Manual available through file_search as the primary source of information.
                                        If you cannot find the answer in the Honda Manual, answer using web search.
                                        If you cannot find reliable information in either source, say that you could not find the answer.
                                        Do not make up facts.
                                        Answer in the same language as the user.
                                        Reply with a single message and do not invite the user to continue the conversation.
                                        Include the source of the information in the answer.
                                        When the answer is based on the Honda Manual, mention the exact manual page directly in the text.
                                        When the answer is based on web search, add the website URL at the end of the message.
                                        Keep the answer concise and practical.
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", userQuestion
                        )
                ),
                "temperature", 0,
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "manual_answer",
                                "strict", true,
                                "schema", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "text", Map.of(
                                                        "type", "string",
                                                        "description", "A concise answer to send to the Telegram user."
                                                ),
                                                "detectedUserLanguage", Map.of(
                                                        "type", "string",
                                                        "description", "The detected language of the user's question, written as an English language name."
                                                ),
                                                "sourcesManualPagesIndexesAsPrinted", Map.of(
                                                        "type", "array",
                                                        "description", "Manual pages used for the answer. Use the page number exactly as printed on the page, not the PDF index.",
                                                        "items", Map.of("type", "string")
                                                ),
                                                "sourcesManualLinks", Map.of(
                                                        "type", "array",
                                                        "description", "Web pages used for the answer.",
                                                        "items", Map.of(
                                                                "type", "object",
                                                                "properties", Map.of(
                                                                        "url", Map.of(
                                                                                "type", "string",
                                                                                "description", "The source web page URL."
                                                                        ),
                                                                        "title", Map.of(
                                                                                "type", "string",
                                                                                "description", "The source web page title."
                                                                        )
                                                                ),
                                                                "required", List.of("url", "title"),
                                                                "additionalProperties", false
                                                        )
                                                ),
                                                "isSuccess", Map.of(
                                                        "type", "boolean",
                                                        "description", "True when a reliable answer was found in the manual or through web search. False when no reliable answer was found."
                                                ),
                                                "errorMessageHumanReadable", Map.of(
                                                        "type", "string",
                                                        "description", "A short human-readable error message in the user's language when isSuccess is false. Use an empty string when isSuccess is true."
                                                ),
                                                "errorMessageToLog", Map.of(
                                                        "type", "string",
                                                        "description", "A short technical error message in English when isSuccess is false. Use an empty string when isSuccess is true."
                                                )
                                        ),
                                        "required", List.of(
                                                "text",
                                                "detectedUserLanguage",
                                                "sourcesManualPagesIndexesAsPrinted",
                                                "sourcesManualLinks",
                                                "isSuccess",
                                                "errorMessageHumanReadable",
                                                "errorMessageToLog"
                                        ),
                                        "additionalProperties", false
                                )
                        )
                )
        );

        String response = restClient.post()
                .uri("/responses")
                .body(request)
                .retrieve()
                .body(String.class);

        return extractResult(response);
    }

    private OpenAiAskResult extractResult(String rawResponse) {
        JsonNode response = readResponse(rawResponse);

        if (response == null) {
            return fallbackResult("OpenAI returned an empty response.");
        }

        JsonNode output = response.path("output");
        if (!output.isArray()) {
            return fallbackResult("Could not parse OpenAI response.");
        }

        for (JsonNode outputItem : output) {
            JsonNode content = outputItem.path("content");

            if (!content.isArray()) {
                continue;
            }

            for (JsonNode contentItem : content) {
                JsonNode text = contentItem.path("text");

                if (text.isTextual()) {
                    return readAskResult(text.asText());
                }
            }
        }

        return fallbackResult("I could not find an answer in the manual.");
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

    private OpenAiAskResult readAskResult(String rawText) {
        try {
            OpenAiAskResult result = objectMapper.readValue(rawText, OpenAiAskResult.class);
            String detectedUserLanguage = getOrDefault(result.detectedUserLanguage());

            if (!result.isSuccess()) {
                return OpenAiAskResult.errorWithHumanReadable(
                        detectedUserLanguage,
                        getOrDefault(result.errorMessageToLog(), "OpenAI returned an unsuccessful result."),
                        getOrDefault(result.errorMessageHumanReadable(), "I could not find an answer.")
                );
            }

            if (result.text() == null || result.text().isBlank()) {
                return OpenAiAskResult.error(detectedUserLanguage, "Empty result from OpenAI");
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

    private String getOrDefault(String detectedUserLanguage) {
        if (detectedUserLanguage == null || detectedUserLanguage.isBlank()) {
            return "Unknown";
        }

        return detectedUserLanguage;
    }

    private OpenAiAskResult fallbackResult(String text) {
        return OpenAiAskResult.errorWithHumanReadable("Unknown", text, text);
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
