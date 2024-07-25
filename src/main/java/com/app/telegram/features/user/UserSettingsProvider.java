package com.app.telegram.features.user;

import com.app.telegram.features.user.storage.FileStorageService;
import com.app.telegram.features.user.storage.StorageService;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Клас, що надає доступ до налаштувань користувачів та керує ними.
 */
public class UserSettingsProvider {
    private static volatile UserSettingsProvider instance;
    private final ConcurrentHashMap<Long, UserSettings> userSettingsMap;
    StorageService storageService = new FileStorageService();

    /**
     * Приватний конструктор для завантаження налаштувань користувачів з файлу.
     */
    public UserSettingsProvider() {
        userSettingsMap = storageService.loadSettings();
    }

    /**
     * Повертає єдиний екземпляр класу `UserSettingsProvider`.
     *
     * @return екземпляр класу `UserSettingsProvider`
     */
    public static UserSettingsProvider getInstance() {
        if (instance == null) {
            synchronized (UserSettingsProvider.class) {
                if (instance == null) {
                    instance = new UserSettingsProvider();
                }
            }
        }
        return instance;
    }

    /**
     * Повертає всі налаштування користувачів.
     *
     * @return мапа з налаштуваннями користувачів
     */
    public ConcurrentHashMap<Long, UserSettings> getAllUserSettings() {
        return userSettingsMap;
    }

    /**
     * Повертає налаштування користувача за його ID чату.
     *
     * @param chatId ID чату користувача
     * @return налаштування користувача
     */
    public UserSettings getUserSettingsById(long chatId) {
        return userSettingsMap.get(chatId);
    }

    /**
     * Встановлює налаштування користувача за його ID чату та зберігає їх у файл.
     *
     * @param chatId ID чату користувача
     * @param userSettings налаштування користувача
     */
    public void setUserSettingsById(Long chatId, UserSettings userSettings) {
        if (chatId == null) {
            System.err.println("Attempted to save user settings with null chatId: " + userSettings);
            return;
        }
        this.userSettingsMap.put(chatId, userSettings);
        storageService.saveSettings(userSettingsMap);
    }

}
