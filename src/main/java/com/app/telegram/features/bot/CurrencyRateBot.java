package com.app.telegram.features.bot;

import com.app.telegram.features.rate.CurrencyRateProvider;
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
        CurrencyRateProvider currencyRateProvider = new CurrencyRateProvider();
        callbackHandler = new CallbackHandler(telegramClient, currencyRateProvider);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String messageText = update.getMessage().getText();
            String userName = update.getMessage().getFrom().getFirstName();

            if (messageText.equals("/start")) {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("Чим я можу допомогти, " + userName + "?")
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
