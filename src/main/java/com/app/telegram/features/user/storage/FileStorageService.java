package com.app.telegram.features.user.storage;

import com.app.telegram.constants.Constants;
import com.app.telegram.features.user.UserSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileStorageService implements StorageService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger LOGGER = LoggerFactory.getLogger(FileStorageService.class);

    @Override
    public void saveSettings(ConcurrentHashMap<Long, UserSettings> userSettings) {
        try {
            objectMapper.writeValue(new File(Constants.USER_SETTINGS_FILE), userSettings);
        } catch (IOException e) {
            LOGGER.error("Error during saving user settings", e);
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
                return objectMapper.readValue(settingsFile, new TypeReference<>() {
                });
            } catch (IOException e) {
                LOGGER.error("Error during loading user settings", e);
                return new ConcurrentHashMap<>();
            }
        }
    }
}