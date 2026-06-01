package com.volodya262.telegram.honda_manual_bot.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum DetectedUserLanguage {
    ENGLISH("en"),
    RUSSIAN("ru"),
    UNKNOWN("unknown");

    private final String isoCode;

    DetectedUserLanguage(String isoCode) {
        this.isoCode = isoCode;
    }

    @JsonValue
    public String isoCode() {
        return isoCode;
    }

    @JsonCreator
    public static DetectedUserLanguage fromIsoCode(String isoCode) {
        if (isoCode == null || isoCode.isBlank()) {
            return UNKNOWN;
        }

        return Arrays.stream(values())
                .filter(language -> language.isoCode.equalsIgnoreCase(isoCode))
                .findFirst()
                .orElse(UNKNOWN);
    }
}
