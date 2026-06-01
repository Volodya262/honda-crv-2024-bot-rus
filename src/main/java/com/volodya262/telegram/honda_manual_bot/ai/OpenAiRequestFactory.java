package com.volodya262.telegram.honda_manual_bot.ai;

import java.util.List;
import java.util.Map;

public class OpenAiRequestFactory {
    public final static String prompt = """
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
                                        """;


    public static Map<String, Object> from(
            String model,
            String vectorStoreId,
            String userQuestion
    ) {
        return Map.of(
                "model", model,
                "tools", List.of(
                        Map.of(
                                "type", "file_search",
                                "vector_store_ids", List.of(vectorStoreId)
                        ),
                        Map.of("type", "web_search")
                ),
                "input", List.of(
                        Map.of(
                                "role", "system",
                                "content", OpenAiRequestFactory.prompt
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
    }
}
