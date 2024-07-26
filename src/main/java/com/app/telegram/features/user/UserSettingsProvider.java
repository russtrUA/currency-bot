package com.app.telegram.features.user;

import com.app.telegram.features.user.storage.FileStorageService;
import com.app.telegram.features.user.storage.StorageService;

import java.util.concurrent.ConcurrentHashMap;


public class UserSettingsProvider {
    private static volatile UserSettingsProvider instance;
    private final ConcurrentHashMap<Long, UserSettings> userSettingsMap;
    StorageService storageService = new FileStorageService();

    private UserSettingsProvider() {
        userSettingsMap = storageService.loadSettings();
    }

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

    public ConcurrentHashMap<Long, UserSettings> getAllUserSettings() {
        return userSettingsMap;
    }

    public UserSettings getUserSettingsById(long chatId) {
        return userSettingsMap.get(chatId);
    }

    public void setUserSettingsById(Long chatId, UserSettings userSettings) {
        this.userSettingsMap.put(chatId, userSettings);
        storageService.saveSettings(userSettingsMap);
    }
}