package com.app.telegram.features.bot;

import com.app.telegram.features.notification.NotificationService;
import com.app.telegram.features.rate.CurrencyRateThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CurrencyRateBot implements LongPollingSingleThreadUpdateConsumer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRateBot.class);
    private final TelegramClient telegramClient;
    private final CallbackHandler callbackHandler;

    public CurrencyRateBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
        callbackHandler = new CallbackHandler(telegramClient);

        CurrencyRateThread currencyRateThread = new CurrencyRateThread();
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(currencyRateThread, 0, 10, TimeUnit.MINUTES);
        new NotificationService(telegramClient).start();
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
                    LOGGER.error("Error sending /start message to chatId {}: {}", chatId, e.getMessage(), e);
                }
            }
        } else if (update.hasCallbackQuery()) {
            callbackHandler.handleCallback(update);
        }
    }
}