package com.app.telegram.features.notification;

import com.app.telegram.features.user.UserSettings;

import java.util.*;
import java.util.concurrent.*;

import static java.util.concurrent.TimeUnit.*;

/**
 * Клас NotificationScheduler використовується для планування та відправки
 * сповіщень користувачам у певний час. Він забезпечує реєстрацію користувачів,
 * оновлення часу сповіщення, скасування сповіщень та управління списками користувачів.
 */
public class NotificationScheduler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    protected final Map<Integer, ScheduledFuture<?>> scheduledTasks = new HashMap<>();
    protected final Map<Integer, List<UserSettings>> usersByHour = new HashMap<>();

    /**
     * Ініціалізує новий екземпляр NotificationScheduler, створюючи пусти
     * списки користувачів для кожної години з 9 до 18.
     */
    public NotificationScheduler() {
        for (int hour = 9; hour <= 18; hour++) {
            usersByHour.put(hour, new ArrayList<>());
        }
    }

    /**
     * Планує завдання для відправки сповіщень у певну годину.
     *
     * @param hour Година, на яку потрібно запланувати сповіщення.
     */
    public void scheduleNotification(int hour) {
        long initialDelay = calculateInitialDelay(hour);
        ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(() -> sendNotifications(hour), initialDelay, 1, DAYS);
        scheduledTasks.put(hour, scheduledTask);
    }

    /**
     * Оновлює час сповіщення для зареєстрованого користувача.
     *
     * @param user Об'єкт UserSettings, який потрібно оновити.
     * @param newHour Нова година для отримання сповіщень.
     */
    public void updateNotificationTime(UserSettings user, int newHour) {
        int oldHour = user.getTimeForNotify();
        removeUserFromHour(user, oldHour);
        user.setTimeForNotify(newHour);
        addUserToHour(user, newHour);

        if (!scheduledTasks.containsKey(newHour)) {
            scheduleNotification(newHour);
        }
    }

    /**
     * Видаляє користувача зі списку користувачів для певної години.
     *
     * @param user Об'єкт UserSettings, який потрібно видалити.
     * @param hour Година, з якої потрібно видалити користувача.
     */
    protected void removeUserFromHour(UserSettings user, int hour) {
        List<UserSettings> users = usersByHour.get(hour);
        if (users != null) {
            users.remove(user);
        }
    }

    /**
     * Додає користувача до списку користувачів для певної години.
     *
     * @param user Об'єкт UserSettings, який потрібно додати.
     * @param hour Година, до якої потрібно додати користувача.
     */
    protected void addUserToHour(UserSettings user, int hour) {
        List<UserSettings> users = usersByHour.get(hour);
        if (users != null) {
            users.add(user);
        }
    }

    /**
     * Скасовує сповіщення для певної години.
     *
     * @param hour Година, для якої потрібно скасувати сповіщення.
     */
    public void cancelNotification(int hour) {
        ScheduledFuture<?> scheduledTask = scheduledTasks.get(hour);
        if (scheduledTask != null) {
            scheduledTask.cancel(true);
            scheduledTasks.remove(hour);
        }
    }

    /**
     * Відправляє сповіщення всім користувачам, які зареєстровані на отримання сповіщень
     * у певну годину.
     *
     * @param hour Година, для якої потрібно відправити сповіщення.
     */
    private void sendNotifications(int hour) {
        List<UserSettings> users = usersByHour.get(hour);
        for (UserSettings user : users) {
            sendNotification(user);
        }
    }

    /**
     * Відправляє сповіщення користувачеві.
     *
     * @param user Об'єкт UserSettings, який отримує сповіщення.
     */
    private void sendNotification(UserSettings user) {

    }

    /**
     * Обчислює початкову затримку до цільової години.
     *
     * @param targetHour Цільова година для сповіщення.
     * @return Початкова затримка в мілісекундах.
     */
    protected long calculateInitialDelay(int targetHour) {
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

    /**
     * Реєструє користувача для отримання сповіщень у певну годину.
     *
     * @param user Об'єкт UserSettings, який містить налаштування користувача для отримання сповіщень.
     */
    public void registerUser(UserSettings user) {
        int hour = user.getTimeForNotify();
        usersByHour.get(hour).add(user);

        if (!scheduledTasks.containsKey(hour)) {
            scheduleNotification(hour);
        }
    }
}
