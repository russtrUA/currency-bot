package com.app.telegram.features.notification;

import com.app.telegram.features.bot.KeyboardFactory;
import com.app.telegram.features.rate.CurrencyRateProvider;
import com.app.telegram.features.user.UserSettings;
import com.app.telegram.features.user.UserSettingsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.*;

public class NotificationService extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);
    private final TelegramClient telegramClient;

    public NotificationService(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    @Override
    public void run() {
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        for (int i = 9; i <= 18; i++) {
            int finalI = i;
            scheduledExecutorService.scheduleAtFixedRate(() -> sendNotification(finalI), calcDelay(finalI), 86400, TimeUnit.SECONDS);
        }
    }

    private long calcDelay(int hour) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime hourToday = now.with(LocalTime.of(hour, 0));
        LocalDateTime hourNextDay = hourToday.plusDays(1);
        if (now.isBefore(hourToday)) {
            return Duration.between(now, hourToday).getSeconds();
        } else {
            return Duration.between(now, hourNextDay).getSeconds();
        }
    }

    private void sendNotification(int hour) {
        ConcurrentHashMap<Long, UserSettings> userSettings = UserSettingsProvider.getInstance().getAllUserSettings();
        CurrencyRateProvider rateProvider = CurrencyRateProvider.getInstance();

        userSettings.entrySet().stream()
                .filter(entry -> entry.getValue().getTimeForNotify() != null)
                .filter(entry -> entry.getValue().getTimeForNotify() == hour)
                .forEach(entry -> {
                    String messageText = rateProvider.getPrettyRatesByChatId(entry.getKey());
                    SendMessage message = SendMessage
                            .builder()
                            .chatId(entry.getKey().toString())
                            .parseMode(ParseMode.HTML)
                            .replyMarkup(KeyboardFactory.getMainKeyboard())
                            .text(messageText)
                            .build();
                    try {
                        telegramClient.execute(message);
                        logger.info("Notification sent to user: {}", entry.getKey());
                    } catch (TelegramApiException e) {
                        logger.error("Failed to send notification to user: {}", entry.getKey(), e);
                    }
                });
    }
}
