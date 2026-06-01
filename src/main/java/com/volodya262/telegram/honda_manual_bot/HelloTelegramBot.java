package com.volodya262.telegram.honda_manual_bot;

import com.volodya262.telegram.honda_manual_bot.config.TelegramBotProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

@Component
public class HelloTelegramBot implements SpringLongPollingBot, LongPollingSingleThreadUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(HelloTelegramBot.class);

    private final TelegramBotProperties properties;
    private final TelegramClient telegramClient;

    private final OpenAiManualQaService openAiManualQaService;

    public HelloTelegramBot(
            TelegramBotProperties properties,
            OpenAiManualQaService openAiManualQaService
    ) {
        this.properties = properties;
        this.telegramClient = new OkHttpTelegramClient(properties.token());
        this.openAiManualQaService = openAiManualQaService;
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

        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        log.info("Received message from chat {}: {}", chatId, text);

        final var openAiResponse = openAiManualQaService.askManual(text);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(openAiResponse)
                .build();

        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message", e);
        }
    }
}