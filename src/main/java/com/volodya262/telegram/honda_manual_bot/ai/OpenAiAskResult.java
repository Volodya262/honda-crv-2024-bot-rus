package com.volodya262.telegram.honda_manual_bot.ai;

import java.util.List;

public record OpenAiAskResult(
        String text,
        DetectedUserLanguage detectedUserLanguage,
        List<String> sourcesManualPagesIndexesAsPrinted,
        List<SourceManualLink> sourcesManualLinks,
        boolean isSuccess,
        String errorMessageToLog,
        String errorMessageHumanReadable
) {

    public static OpenAiAskResult success(
            String text,
            DetectedUserLanguage detectedUserLanguage,
            List<String> sourcesManualPagesIndexesAsPrinted,
            List<SourceManualLink> sourcesManualLinks
    ) {
        return new OpenAiAskResult(
                text,
                detectedUserLanguage,
                sourcesManualPagesIndexesAsPrinted,
                sourcesManualLinks,
                true,
                null,
                null
        );
    }

    public static OpenAiAskResult error(
            DetectedUserLanguage detectedUserLanguage,
            String errorMessageToLog
    ) {
        return new OpenAiAskResult(
                null,
                detectedUserLanguage,
                List.of(),
                List.of(),
                false,
                errorMessageToLog,
                null
        );
    }

    public static OpenAiAskResult errorWithHumanReadable(
            DetectedUserLanguage detectedUserLanguage,
            String errorMessageToLog,
            String errorMessageHumanReadable
    ) {
        return new OpenAiAskResult(
                null,
                detectedUserLanguage,
                List.of(),
                List.of(),
                false,
                errorMessageToLog,
                errorMessageHumanReadable
        );
    }

    public record SourceManualLink(String url, String title) {
    }
}
