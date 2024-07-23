package com.app.telegram.features.notification;

import com.app.telegram.features.user.UserSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

import static org.junit.jupiter.api.Assertions.*;

public class NotificationSchedulerTest {

    private NotificationScheduler notificationScheduler;
    private UserSettings userSettings;

    @BeforeEach
    public void setUp() {
        notificationScheduler = new NotificationScheduler();
        userSettings = new UserSettings();
        userSettings.setTimeForNotify(10);
    }

    @Test
    public void testRegisterUser() {
        notificationScheduler.registerUser(userSettings);

        List<UserSettings> users = notificationScheduler.usersByHour.get(10);
        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(userSettings, users.getFirst());
    }

    @Test
    public void testRegisterMultipleUsersInSameHour() {
        UserSettings anotherUser = new UserSettings();
        anotherUser.setTimeForNotify(10);

        notificationScheduler.registerUser(userSettings);
        notificationScheduler.registerUser(anotherUser);

        List<UserSettings> users = notificationScheduler.usersByHour.get(10);
        assertNotNull(users);
        assertEquals(2, users.size());
        assertTrue(users.contains(userSettings));
        assertTrue(users.contains(anotherUser));
    }

    @Test
    public void testRegisterUsersInDifferentHours() {
        UserSettings anotherUser = new UserSettings();
        anotherUser.setTimeForNotify(11);

        notificationScheduler.registerUser(userSettings);
        notificationScheduler.registerUser(anotherUser);

        List<UserSettings> usersAt10 = notificationScheduler.usersByHour.get(10);
        List<UserSettings> usersAt11 = notificationScheduler.usersByHour.get(11);

        assertNotNull(usersAt10);
        assertNotNull(usersAt11);
        assertEquals(1, usersAt10.size());
        assertEquals(1, usersAt11.size());
        assertTrue(usersAt10.contains(userSettings));
        assertTrue(usersAt11.contains(anotherUser));
    }

    @Test
    public void testUpdateNotificationTime() {
        notificationScheduler.registerUser(userSettings);

        List<UserSettings> initialHourUsers = notificationScheduler.usersByHour.get(10);
        assertNotNull(initialHourUsers);
        assertTrue(initialHourUsers.contains(userSettings));
        assertEquals(1, initialHourUsers.size());

        int oldHour = userSettings.getTimeForNotify();
        int newHour = 14;
        notificationScheduler.updateNotificationTime(userSettings, newHour);

        List<UserSettings> oldHourUsers = notificationScheduler.usersByHour.get(oldHour);
        assertNotNull(oldHourUsers);
        assertFalse(oldHourUsers.contains(userSettings));

        List<UserSettings> newHourUsers = notificationScheduler.usersByHour.get(newHour);
        assertNotNull(newHourUsers);
        assertTrue(newHourUsers.contains(userSettings));
        assertEquals(1, newHourUsers.size());
    }

    @Test
    public void testRemoveUserFromHour() {
        notificationScheduler.registerUser(userSettings);
        notificationScheduler.removeUserFromHour(userSettings, 10);

        List<UserSettings> users = notificationScheduler.usersByHour.get(10);
        assertNotNull(users);
        assertEquals(0, users.size(), "Users should be empty after removal");
    }

    @Test
    public void testAddUserToHour() {
        notificationScheduler.addUserToHour(userSettings, 14);

        List<UserSettings> users = notificationScheduler.usersByHour.get(14);
        assertNotNull(users);
        assertEquals(1, users.size(), "Users should contain one user after addition");
        assertEquals(userSettings, users.getFirst());
    }

    @Test
    public void testCancelNotification() {
        notificationScheduler.registerUser(userSettings);

        notificationScheduler.cancelNotification(10);
        ScheduledFuture<?> scheduledTask = notificationScheduler.scheduledTasks.get(10);
        assertNull(scheduledTask);

        List<UserSettings> users = notificationScheduler.usersByHour.get(10);
        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    public void testCancelNotificationAfterRemovingAllUsers() {
        notificationScheduler.registerUser(userSettings);
        notificationScheduler.removeUserFromHour(userSettings, 10);
        notificationScheduler.cancelNotification(10);

        ScheduledFuture<?> scheduledTask = notificationScheduler.scheduledTasks.get(10);
        assertNull(scheduledTask);

        List<UserSettings> users = notificationScheduler.usersByHour.get(10);
        assertNotNull(users);
        assertEquals(0, users.size());
    }

    @Test
    public void testCalculateInitialDelay() {
        long delay = notificationScheduler.calculateInitialDelay(10);
        Calendar current = Calendar.getInstance();

        Calendar target = (Calendar) current.clone();
        target.set(Calendar.HOUR_OF_DAY, 10);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.before(current)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }

        long expectedDelay = target.getTimeInMillis() - current.getTimeInMillis();

        long tolerance = 1000;
        assertTrue(Math.abs(delay - expectedDelay) < tolerance);
    }
}
