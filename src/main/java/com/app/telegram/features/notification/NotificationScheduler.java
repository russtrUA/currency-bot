package com.app.telegram.features.notification;

import com.app.telegram.features.bot.KeyboardFactory;
import com.app.telegram.features.rate.CurrencyRateProvider;
import com.app.telegram.features.user.UserSettings;
import com.app.telegram.features.user.UserSettingsProvider;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class NotificationScheduler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final UserSettingsProvider userSettingsProvider;
    private final Map<Integer, CopyOnWriteArrayList<UserSettings>> usersByHour = new ConcurrentHashMap<>();
    private final TelegramClient telegramClient;

    public NotificationScheduler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.userSettingsProvider = UserSettingsProvider.getInstance();

        for (int hour = 9; hour <= 18; hour++) {
            usersByHour.put(hour, new CopyOnWriteArrayList<>());
        }

        loadUsersSettings();
        scheduleNotifications();
    }

    public void loadUsersSettings() {
        userSettingsProvider.getAllUserSettings()
                .forEach((chatId, userSettings) -> {
                    userSettings.setChatId(chatId); // Встановлюємо chatId у UserSettings
                    addUserToHour(userSettings, userSettings.getTimeForNotify());
                });
    }

    public void addUserToHour(UserSettings user, Integer hour) {
        if (hour != null) {
            List<UserSettings> users = usersByHour.get(hour);
            users.add(user);
        }
    }

    public void updateNotificationTime(UserSettings user, int newHour) {
        removeUserFromHour(user, user.getTimeForNotify());
        user.setTimeForNotify(newHour);
        addUserToHour(user, newHour);
        userSettingsProvider.setUserSettingsById(user.getChatId(), user);
    }

    public void removeUserFromHour(UserSettings user, Integer hour) {
        if (hour != null) {
            List<UserSettings> users = usersByHour.get(hour);
            users.remove(user);
        }
        user.setTimeForNotify(null);
        userSettingsProvider.setUserSettingsById(user.getChatId(), user);
    }

    public long calculateInitialDelay(int hour) {
        LocalTime now = LocalTime.now();
        LocalTime targetTime = LocalTime.of(hour, 0);

        if (now.isAfter(targetTime)) {
            targetTime = targetTime.plusHours(24);
        }

        return now.until(targetTime, ChronoUnit.SECONDS);
    }

    void scheduleNotifications() {
        for (int hour = 9; hour <= 18; hour++) {
            int finalHour = hour;
            long initialDelay = calculateInitialDelay(hour);
            scheduler.scheduleAtFixedRate(() -> sendNotification(finalHour), initialDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
        }
    }

    private void sendNotification(int hour) {
        List<UserSettings> users = usersByHour.get(hour);
        CurrencyRateProvider rateProvider = CurrencyRateProvider.getInstance();

        for (UserSettings user : users) {
            String messageText = rateProvider.getPrettyRatesByChatId(user.getChatId());
            SendMessage message = SendMessage
                    .builder()
                    .chatId(user.getChatId())
                    .parseMode(ParseMode.HTML)
                    .replyMarkup(KeyboardFactory.getMainKeyboard())
                    .text(messageText)
                    .build();
            try {
                telegramClient.execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}
