package com.app.telegram.features.user.storage;

import com.app.telegram.features.user.UserSettings;

import java.util.concurrent.ConcurrentHashMap;

public interface StorageService {
    void saveSettings(ConcurrentHashMap<Long, UserSettings> userSettings);
    ConcurrentHashMap<Long, UserSettings> loadSettings();

}