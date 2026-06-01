package com.volodya262.telegram.honda_manual_bot.context;

import com.volodya262.telegram.honda_manual_bot.context.UserContext.UserContextKey;

import java.util.Optional;

public interface UserContextStorage {

    UserContext appendMessage(UserContextKey key, UserInfo userInfo, ChatMessage message);

    Optional<UserContext> findByKey(UserContextKey key);
}
