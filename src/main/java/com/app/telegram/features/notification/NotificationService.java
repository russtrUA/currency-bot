package com.app.telegram.features.notification;

import com.app.telegram.features.user.UserSettings;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class NotificationService {
    private final NotificationScheduler notificationScheduler;

    public NotificationService(TelegramClient telegramClient) {
        this.notificationScheduler = new NotificationScheduler(telegramClient);

        startScheduler();
    }

    public void startScheduler() {
        notificationScheduler.loadUsersSettings();
        notificationScheduler.scheduleNotifications();
    }

    public void updateUserNotificationTime(UserSettings user, int newHour) {
        notificationScheduler.updateNotificationTime(user, newHour);
    }

    public void removeUser(UserSettings user, int hour) {
        notificationScheduler.removeUserFromHour(user, hour);
    }
}
