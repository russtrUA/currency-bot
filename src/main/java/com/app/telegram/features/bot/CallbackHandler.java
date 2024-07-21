package com.app.telegram.features.bot;

import com.app.telegram.features.user.UserSettings;
import com.app.telegram.features.user.UserSettingsProvider;

import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class CallbackHandler {
    private final TelegramClient telegramClient;
    private final UserSettingsProvider userSettingsProvider;

    public CallbackHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.userSettingsProvider = new UserSettingsProvider();
    }

    public void handleCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        long userId = update.getCallbackQuery().getFrom().getId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        UserSettings userSettings = userSettingsProvider.getUserSettings(userId);

        switch (callbackData) {
            case "settings":
                sendSettingsKeyboard(chatId);
                break;
            case "back_to_menu":
                sendMainKeyboard(chatId, "How can I help you?");
                break;
            case "get_info":
                // Implement info retrieval from CurrencyRateProvider
                break;
            case "bank":
                sendBankSettingsKeyboard(chatId, userSettings);
                break;
            case "currency":
                sendCurrencySettingsKeyboard(chatId, userSettings);
                break;
            case "number_of_decimal_places":
                sendDecimalPlacesSettingsKeyboard(chatId, userSettings);
                break;
            case "notifications":
                sendNotificationsSettingsKeyboard(chatId, userSettings);
                break;
            case "back":
                sendSettingsKeyboard(chatId);
                break;
            case "PrivatBank":
            case "Monobank":
            case "NBU":
                updateBankSetting(userId, callbackData, chatId, messageId, userSettings);
                break;
            case "USD":
            case "EUR":
                updateCurrencySetting(userId, callbackData, chatId, messageId, userSettings);
                break;
            case "2":
            case "3":
            case "4":
                updateDecimalPlacesSetting(userId, Integer.parseInt(callbackData), chatId, messageId, userSettings);
                break;
            default:
                if (callbackData.matches("\\d{2}:00")) {
                    updateNotificationTime(userId, callbackData, chatId, messageId, userSettings);
                } else if (callbackData.equals("Disable notifications")) {
                    updateNotificationTime(userId, null, chatId, messageId, userSettings);
                }
                break;
        }
    }

    private void sendMainKeyboard(long chatId, String text) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(KeyboardFactory.getMainKeyboard())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendSettingsKeyboard(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Settings")
                .replyMarkup(KeyboardFactory.getSettingsKeyboard())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendBankSettingsKeyboard(long chatId, UserSettings userSettings) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose a bank:")
                .replyMarkup(KeyboardFactory.getBankSettingsKeyboard(userSettings))
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendCurrencySettingsKeyboard(long chatId, UserSettings userSettings) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose a currency:")
                .replyMarkup(KeyboardFactory.getCurrencySettingsKeyboard(userSettings))
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDecimalPlacesSettingsKeyboard(long chatId, UserSettings userSettings) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose the number of decimal places:")
                .replyMarkup(KeyboardFactory.getDecimalPlacesSettingsKeyboard(userSettings))
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationsSettingsKeyboard(long chatId, UserSettings userSettings) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose notification time:")
                .replyMarkup(KeyboardFactory.getNotificationsSettingsKeyboard(userSettings))
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void updateBankSetting(long userId, String bankName, long chatId, int messageId, UserSettings userSettings) {
        userSettings.setChosenBanks(List.of(Bank.valueOf(bankName)));
        userSettingsProvider.updateUserSettings(userId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getBankSettingsKeyboard(userSettings));
    }

    private void updateCurrencySetting(long userId, String currencyName, long chatId, int messageId, UserSettings userSettings) {
        List<Currency> currencies = new ArrayList<>(userSettings.getChosenCurrencies());
        Currency currency = Currency.valueOf(currencyName);
        if (currencies.contains(currency)) {
            currencies.remove(currency);
        } else {
            currencies.add(currency);
        }
        userSettings.setChosenCurrencies(currencies);
        userSettingsProvider.updateUserSettings(userId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getCurrencySettingsKeyboard(userSettings));
    }

    private void updateDecimalPlacesSetting(long userId, int decimalPlaces, long chatId, int messageId, UserSettings userSettings) {
        userSettings.setChosenCountSigns(decimalPlaces);
        userSettingsProvider.updateUserSettings(userId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getDecimalPlacesSettingsKeyboard(userSettings));
    }

    private void updateNotificationTime(long userId, String time, long chatId, int messageId, UserSettings userSettings) {
        userSettings.setTimeForNotify(time != null ? Integer.parseInt(time.split(":")[0]) : null);
        userSettingsProvider.updateUserSettings(userId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getNotificationsSettingsKeyboard(userSettings));
    }

    private void updateKeyboard(long chatId, int messageId, InlineKeyboardMarkup keyboard) {
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(keyboard)
                .build();
        try {
            telegramClient.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}