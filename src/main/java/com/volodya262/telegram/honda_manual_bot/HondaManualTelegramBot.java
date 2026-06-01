package com.volodya262.telegram.honda_manual_bot;

import com.volodya262.telegram.honda_manual_bot.ai.OpenAiManualQaService;
import com.volodya262.telegram.honda_manual_bot.ai.OpenAiAskResult;
import com.volodya262.telegram.honda_manual_bot.config.TelegramBotProperties;
import com.volodya262.telegram.honda_manual_bot.context.UserContext.UserContextKey;
import com.volodya262.telegram.honda_manual_bot.context.UserContextService;
import com.volodya262.telegram.honda_manual_bot.context.UserInfo;
import com.volodya262.telegram.honda_manual_bot.domain.DetectedUserLanguage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.Instant;

@Component
public class HondaManualTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(HondaManualTelegramBot.class);
    private static final String DEFAULT_ERROR_MESSAGE = "Sorry, I could not prepare an answer.";
    private static final String RATE_LIMIT_EXCEEDED_MESSAGE = "Rate limit exceed";

    private final TelegramBotProperties properties;
    private final TelegramClient telegramClient;

    private final OpenAiManualQaService openAiManualQaService;
    private final UserContextService userContextService;

    public HondaManualTelegramBot(
            TelegramBotProperties properties,
            OpenAiManualQaService openAiManualQaService,
            UserContextService userContextService
    ) {
        this.properties = properties;
        this.telegramClient = new OkHttpTelegramClient(properties.token());
        this.openAiManualQaService = openAiManualQaService;
        this.userContextService = userContextService;
    }

    @Override
    public String getBotToken() {
        return properties.token();
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return this;
    }

    @Override
    public void consume(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Message telegramMessage = update.getMessage();
        Long chatId = telegramMessage.getChatId();
        String text = telegramMessage.getText();
        UserContextKey contextKey = contextKey(telegramMessage);
        UserInfo userInfo = userInfo(telegramMessage);

        log.info("Message from chat {}: {}", chatId, text);

        userContextService.addUserMessage(
                contextKey, userInfo, "telegram:%s".formatted(telegramMessage.getMessageId()), text, messageTimestamp(telegramMessage)
        );

        if (!userContextService.checkRateLimits(contextKey)) {
            sendMessage(chatId, RATE_LIMIT_EXCEEDED_MESSAGE);
            log.warn("Rate limit exceeded. [chatId: {}, userId: {}]", contextKey.chatId(), contextKey.userId());
            return;
        }

        final var openAiResponse = openAiManualQaService.askManual(text); // передавай сюда user context аргументом
        log.info("OpenAI response received. [chatId: {}, userMessage: {}, openAiResponse: {}]", chatId, text, openAiResponse);

        final var messageForUser = buildMessageTextForUser(openAiResponse);
        if (sendMessage(chatId, messageForUser)) {
            userContextService.addAssistantMessage(contextKey, userInfo, messageForUser);
        }
    }

    private boolean sendMessage(Long chatId, String text) {
        final var message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();

        try {
            telegramClient.execute(message);
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message", e);
            return false;
        }
    }

    private String buildMessageTextForUser(OpenAiAskResult openAiResponse) {
        if (openAiResponse.isSuccess()) {
            return openAiResponse.text();
        }

        if (openAiResponse.errorMessageHumanReadable() != null && !openAiResponse.errorMessageHumanReadable().isBlank()) {
            return openAiResponse.errorMessageHumanReadable();
        }

        return DEFAULT_ERROR_MESSAGE;
    }

    private UserContextKey contextKey(Message message) {
        Long chatId = message.getChatId();
        if (chatId == null) {
            throw new IllegalArgumentException("Telegram chat id must be present.");
        }

        User from = message.getFrom();
        if (from == null) {
            throw new IllegalArgumentException("Telegram message author must be present.");
        }

        Long userId = from.getId();
        if (userId == null) {
            throw new IllegalArgumentException("Telegram message author id must be present.");
        }

        return new UserContextKey(chatId, userId);
    }

    private UserInfo userInfo(Message message) {
        User from = message.getFrom();

        if (from == null) {
            throw new IllegalArgumentException("Telegram message author must be present.");
        }

        return new UserInfo(
                from.getId(),
                from.getUserName(),
                from.getFirstName(),
                from.getLastName(),
                DetectedUserLanguage.fromIsoCode(from.getLanguageCode())
        );
    }

    private Instant messageTimestamp(Message message) {
        Integer date = message.getDate();

        if (date == null) {
            return Instant.now();
        }

        return Instant.ofEpochSecond(date);
    }
}
