package com.app.telegram.features.bot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class CurrencyRateBot implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;
    private final CallbackHandler callbackHandler;


    public CurrencyRateBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        callbackHandler = new CallbackHandler(telegramClient);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chat_id = update.getMessage().getChatId();
            String message_text = update.getMessage().getText();
            String user_name = update.getMessage().getFrom().getFirstName();

            if (message_text.equals("/start")) {
                SendMessage message = SendMessage.builder()
                        .chatId(chat_id)
                        .text("How can I help you, " + user_name + "?")
                        .replyMarkup(KeyboardFactory.getMainKeyboard())
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallback(update);
        }
    }
}
