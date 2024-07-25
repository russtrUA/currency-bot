package com.app.telegram.features.notification;

import com.app.telegram.features.rate.CurrencyRateProvider;
import com.app.telegram.features.user.UserSettings;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    private final TelegramClient telegramClient;
    private final CurrencyRateProvider currencyRateProvider;

    public NotificationService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.currencyRateProvider = CurrencyRateProvider.getInstance();
    }

    public void sendNotification(UserSettings userSettings) {
        if (userSettings == null) {
            LOGGER.error("UserSettings is null");
            return;
        }

        long chatId = userSettings.getChatId();
        String messageText = currencyRateProvider.getPrettyRatesByChatId(chatId);

        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(messageText)
                .build();

        try {
            telegramClient.execute(message);
            LOGGER.info("Notification sent to chatId {}: {}", chatId, messageText);
            Thread.sleep(100);
        } catch (TelegramApiException e) {
            LOGGER.error("Error sending notification to chatId {}: {}", chatId, e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
