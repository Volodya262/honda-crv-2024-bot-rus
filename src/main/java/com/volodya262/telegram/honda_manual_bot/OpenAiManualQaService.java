package com.volodya262.telegram.honda_manual_bot;

import com.fasterxml.jackson.databind.JsonNode;
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
    private final RestClient restClient;

    public OpenAiManualQaService(OpenAiProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + properties.apiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public String askManual(String userQuestion) {
        Map<String, Object> request = Map.of(
                "model", properties.model(),
                "tools", List.of(
                        Map.of(
                                "type", "file_search",
                                "vector_store_ids", List.of(properties.vectorStoreId())
                        )
                ),
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", """
                                        You are an assistant for answering questions about the Honda manual.
                                        Answer only using the manual content when possible.
                                        If the manual does not contain the answer, say that you could not find it in the manual.
                                        Keep answers concise and practical.
                                        """
                        ),
                        Map.of(
                                "role", "user",
                                "content", userQuestion
                        )
                )
        );

        String response = restClient.post()
                .uri("/responses")
                .body(request)
                .retrieve()
                .body(String.class);

        return response;
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            return "OpenAI returned an empty response.";
        }

        JsonNode output = response.path("output");
        if (!output.isArray()) {
            return "Could not parse OpenAI response.";
        }

        StringBuilder result = new StringBuilder();

        for (JsonNode outputItem : output) {
            JsonNode content = outputItem.path("content");

            if (!content.isArray()) {
                continue;
            }

            for (JsonNode contentItem : content) {
                JsonNode text = contentItem.path("text");

                if (text.isTextual()) {
                    result.append(text.asText()).append("\n");
                }
            }
        }

        String answer = result.toString().trim();

        if (answer.isBlank()) {
            return "I could not find an answer in the manual.";
        }

        return answer;
    }
}
