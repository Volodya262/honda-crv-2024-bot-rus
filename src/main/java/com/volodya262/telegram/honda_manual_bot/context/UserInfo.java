package com.volodya262.telegram.honda_manual_bot.context;

import com.volodya262.telegram.honda_manual_bot.domain.DetectedUserLanguage;

public record UserInfo(
        Long id,
        String username,
        String firstName,
        String lastName,
        DetectedUserLanguage language
) {
}
