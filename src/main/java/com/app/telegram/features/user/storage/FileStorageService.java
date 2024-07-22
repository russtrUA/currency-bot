package com.app.telegram.features.user.storage;

import com.app.telegram.constants.Constants;
import com.app.telegram.features.user.UserSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;


public class FileStorageService implements StorageService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectWriter objectWriter = objectMapper.writer();


    @Override
    public void saveSettings(ConcurrentHashMap<Long, UserSettings> userSettings) {
        try {
            objectWriter.writeValue(new File(Constants.USER_SETTINGS_FILE), userSettings);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save user settings", e);
        }
    }

    @Override
    public ConcurrentHashMap<Long, UserSettings> loadSettings() {
        File settingsFile = Paths.get(Constants.USER_SETTINGS_FILE).toFile();

        if (!settingsFile.exists()) {
            ConcurrentHashMap<Long, UserSettings> defaultSettings = new ConcurrentHashMap<>();
            saveSettings(defaultSettings);
            return defaultSettings;
        } else {
            try {
                return objectMapper.readValue(Constants.USER_SETTINGS_FILE, new TypeReference<>() {
                });
            } catch (IOException e) {
                throw new RuntimeException("Failed to load user settings", e);
            }
        }
    }
}