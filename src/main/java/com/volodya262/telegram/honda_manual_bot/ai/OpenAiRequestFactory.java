package com.volodya262.telegram.honda_manual_bot.ai;

import com.volodya262.telegram.honda_manual_bot.context.ChatMessage;
import com.volodya262.telegram.honda_manual_bot.context.MessageRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OpenAiRequestFactory {
    private static final int MAX_OUTPUT_TOKENS = 1200;
    private static final int MAX_TOOL_CALLS = 2;

    public final static String prompt = """
                                        Your task is to answer the user's question about the Dongfeng Honda CR-V 6.
                                        Only answer questions that are directly related to the Dongfeng Honda CR-V 6, its manual, operation, maintenance, features, warnings, troubleshooting, or ownership.
                                        A request is out of scope when Honda CR-V is only incidental context, such as a location where the user wants to do an unrelated task.
                                        If the user asks for anything outside that scope, do not answer the unrelated request.
                                        For out-of-scope requests, politely say in the user's language that you can only help with questions about the Dongfeng Honda CR-V, set isSuccess to false, use an empty text if appropriate, provide a human-readable error message, and leave source arrays empty.
                                        Use the Honda manuals available through file_search as the primary source of information.
                                        When file_search returns relevant information from both manuals, prefer Honda CRV China manual.pdf first, then Honda CRV 2024 manual.pdf.
                                        If the Chinese manual conflicts with the English manual, follow the Chinese manual.
                                        Use web search only when neither the Chinese manual nor the English manual in file_search provides a reliable answer.
                                        If the answer is found in the Honda Manual, do not use web search and keep sourcesManualLinks empty.
                                        If you cannot find reliable information in either source, say that you could not find the answer.
                                        Do not make up facts.
                                        Answer in the same language as the user.
                                        Set detectedUserLanguage to the user's language ISO code: en, ru, or unknown.
                                        Reply with a single message and do not invite the user to continue the conversation.
                                        Include the source of the information in the answer.
                                        When the answer is based on the Honda Manual, mention the exact manual page directly in the text.
                                        When the answer is based on web search, add the website URL at the end of the message.
                                        Keep the answer concise and practical.
                                        """;


    public static Map<String, Object> from(
            String model,
            String vectorStoreId,
            List<ChatMessage> messages
    ) {
        return Map.ofEntries(
                Map.entry("model", model),
                Map.entry("tools", List.of(
                        Map.of(
                                "type", "file_search",
                                "vector_store_ids", List.of(vectorStoreId)
                        ),
                        Map.of("type", "web_search")
                )),
                Map.entry("input", input(messages)),
                Map.entry("temperature", 0),
                Map.entry("max_output_tokens", MAX_OUTPUT_TOKENS),
                Map.entry("max_tool_calls", MAX_TOOL_CALLS),
                Map.entry("tool_choice", "auto"),
                Map.entry("parallel_tool_calls", false),
                Map.entry("truncation", "auto"),
                Map.entry("store", false),
                Map.entry("text", Map.of(
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
                                                        "description", "The detected language of the user's question as an ISO code. Use en for English, ru for Russian, or unknown when unsure.",
                                                        "enum", List.of("en", "ru", "unknown")
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
                ))
        );
    }

    private static List<Map<String, String>> input(List<ChatMessage> messages) {
        List<Map<String, String>> input = new ArrayList<>();
        input.add(Map.of(
                "role", "system",
                "content", OpenAiRequestFactory.prompt
        ));

        input.addAll(messages.stream()
                .filter(message -> message.text() != null && !message.text().isBlank())
                .map(OpenAiRequestFactory::inputMessage)
                .toList());

        return input;
    }

    private static Map<String, String> inputMessage(ChatMessage message) {
        return Stream.of(
                Map.entry("role", role(message.role())),
                Map.entry("content", message.text())
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static String role(MessageRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
        };
    }
}
