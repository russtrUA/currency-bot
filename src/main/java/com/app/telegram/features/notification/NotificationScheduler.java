package com.app.telegram.features.notification;

import com.app.telegram.features.user.UserSettings;
import com.app.telegram.features.user.UserSettingsProvider;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;
import java.util.concurrent.*;

public class NotificationScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final Map<Integer, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    private final Map<Integer, List<UserSettings>> usersByHour = new HashMap<>();
    private final NotificationService notificationService;
    private final UserSettingsProvider userSettingsProvider;

    public NotificationScheduler(TelegramClient telegramClient) {
        this.notificationService = new NotificationService(telegramClient);
        this.userSettingsProvider = UserSettingsProvider.getInstance();

        for (int hour = 9; hour <= 18; hour++) {
            usersByHour.put(hour, new ArrayList<>());
        }

        loadUsersForNotifications();

        scheduleAllNotifications();
    }

    private void loadUsersForNotifications() {
        userSettingsProvider.getAllUserSettings().forEach((chatId, userSettings) -> {
            if (userSettings == null) {
                return;
            }
            Integer hour = userSettings.getTimeForNotify();
            if (hour != null) {
                addUserToHour(userSettings, hour);
                System.out.println("Loaded user for notification at hour " + hour + ": " + chatId);  // Додано логування
            }
        });
    }

    private void scheduleAllNotifications() {
        for (int hour : usersByHour.keySet()) {
            if (!usersByHour.get(hour).isEmpty()) {
                scheduleNotification(hour);
            }
        }
    }

    public void scheduleNotification(int hour) {
        long initialDelay = calculateInitialDelay(hour);
        ScheduledFuture<?> scheduledTask = scheduler.schedule(
                () -> sendAndRescheduleNotification(hour),
                initialDelay,
                TimeUnit.MILLISECONDS
        );
        scheduledTasks.put(hour, scheduledTask);
        System.out.println("Scheduled notification for hour: " + hour);  // Додано логування
    }

    public void updateNotificationTime(UserSettings user, int newHour) {
        Integer oldHour = user.getTimeForNotify();
        if (oldHour != null) {
            removeUserFromHour(user, oldHour);
        }
        user.setTimeForNotify(newHour);
        addUserToHour(user, newHour);

        if (!scheduledTasks.containsKey(newHour)) {
            scheduleNotification(newHour);
        }
    }

    private void removeUserFromHour(UserSettings user, int hour) {
        List<UserSettings> users = usersByHour.get(hour);
        if (users != null) {
            users.remove(user);
        }
    }

    private void addUserToHour(UserSettings user, int hour) {
        List<UserSettings> users = usersByHour.get(hour);
        if (users != null) {
            users.add(user);
            System.out.println("Added user to hour " + hour + ": " + user.getChatId());  // Додано логування
        }
    }

    private void sendAndRescheduleNotification(int hour) {
        sendNotifications(hour);
        scheduleNotification(hour);
    }

    private void sendNotifications(int hour) {
        List<UserSettings> users = usersByHour.get(hour);
        System.out.println("Sending notifications for hour: " + hour);  // Додано логування
        for (UserSettings user : users) {
            System.out.println("Sending notification to user: " + user.getChatId());  // Додано логування
            notificationService.sendNotification(user);
        }
    }

    long calculateInitialDelay(int targetHour) {
        Calendar current = Calendar.getInstance();

        Calendar target = (Calendar) current.clone();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.before(current)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        return target.getTimeInMillis() - current.getTimeInMillis();
    }

    public void registerUser(UserSettings user) {
        int hour = user.getTimeForNotify();
        usersByHour.get(hour).add(user);

        if (!scheduledTasks.containsKey(hour)) {
            scheduleNotification(hour);
        }
    }

    public void sendDailyNotifications() {
        Calendar current = Calendar.getInstance();
        int hour = current.get(Calendar.HOUR_OF_DAY);
        if (usersByHour.containsKey(hour)) {
            sendNotifications(hour);
        }
    }
}
