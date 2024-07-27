package com.app.telegram.features.bot;

import com.app.telegram.features.rate.CurrencyRateProvider;
import com.app.telegram.features.user.UserSettings;
import com.app.telegram.features.user.UserSettingsProvider;
import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.List;

public class CallbackHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(CallbackHandler.class);

    private final TelegramClient telegramClient;
    private final UserSettingsProvider userSettingsProvider;
    private final CurrencyRateProvider currencyRateProvider;

    public CallbackHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
        this.userSettingsProvider = UserSettingsProvider.getInstance();
        this.currencyRateProvider = CurrencyRateProvider.getInstance();
    }

    public void handleCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        LOGGER.info("Received callback: data={}, chatId={}, messageId={}", callbackData, chatId, messageId);

        UserSettings userSettings = userSettingsProvider.getUserSettingsById(chatId);
        if (userSettings == null) {
            LOGGER.warn("User settings not found for chatId={}, using default settings", chatId);
            userSettings = new UserSettings();
            userSettingsProvider.setUserSettingsById(chatId, userSettings);
        } else {
            LOGGER.info("Loaded user settings for chatId={}", chatId);
        }

        switch (callbackData) {
            case "settings", "back":
                sendSettingsKeyboard(chatId);
                break;
            case "back_to_menu":
                sendMainKeyboard(chatId, "Чим я можу допомогти?");
                break;
            case "get_info":
                getCurrentSettings(userSettings);
                handleGetInfo(chatId, userSettings);
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
            default:
                handleDynamicCallback(callbackData, chatId, messageId, userSettings);
                break;
        }
    }

    private void handleDynamicCallback(String callbackData, long chatId, int messageId, UserSettings userSettings) {
        try {
            if (Bank.isValidBank(callbackData)) {
                updateBankSetting(chatId, callbackData, messageId, userSettings);
            } else if (Currency.isValidCurrency(callbackData)) {
                updateCurrencySetting(chatId, callbackData, messageId, userSettings);
            } else if (callbackData.matches("\\d")) {
                updateDecimalPlacesSetting(chatId, Integer.parseInt(callbackData), messageId, userSettings);
            } else if (callbackData.matches("\\d{2}:00")) {
                updateNotificationTime(chatId, callbackData, messageId, userSettings);
            } else if (callbackData.equals("Disable notifications")) {
                updateNotificationTime(chatId, null, messageId, userSettings);
            }
        } catch (IllegalArgumentException e) {
            LOGGER.error("Invalid callback data: {}", callbackData, e);
        }
    }

    private void getCurrentSettings(UserSettings userSettings) {
        userSettings.getChosenBanks().forEach(bank ->
                LOGGER.info("Chosen bank: {}", bank)
        );
        userSettings.getChosenCurrencies().forEach(currency ->
                LOGGER.info("Chosen currency: {}", currency)
        );
        LOGGER.info("Decimal places: {}", userSettings.getChosenCountSigns());
        LOGGER.info("Notification time: {}", userSettings.getTimeForNotify() != null
                ? userSettings.getTimeForNotify() + ":00"
                : "Notifications disabled"
        );
    }

    private void handleGetInfo(long chatId, UserSettings userSettings) {
        List<Currency> chosenCurrencies = userSettings.getChosenCurrencies();
        List<Bank> chosenBanks = userSettings.getChosenBanks();

        if (chosenCurrencies.isEmpty()) {
            sendMessage(chatId, "Не обрано жодної валюти:", KeyboardFactory.getDynamicCurrencySettingsKeyboard(userSettings));
            return;
        }

        if (chosenBanks.isEmpty()) {
            sendMessage(chatId, "Не обрано жодного банку:", KeyboardFactory.getDynamicBankSettingsKeyboard(userSettings));
            return;
        }

        String ratesMessage = currencyRateProvider.getPrettyRatesByChatId(chatId);
        sendMessage(chatId, ratesMessage, KeyboardFactory.getMainKeyboard());
    }

    private void sendMainKeyboard(long chatId, String text) {
        sendMessage(chatId, text, KeyboardFactory.getMainKeyboard());
    }

    private void sendSettingsKeyboard(long chatId) {
        sendMessage(chatId, "Виберіть налаштування:", KeyboardFactory.getSettingsKeyboard());
    }

    private void sendBankSettingsKeyboard(long chatId, UserSettings userSettings) {
        sendMessage(chatId, "Виберіть банк:", KeyboardFactory.getDynamicBankSettingsKeyboard(userSettings));
    }

    private void sendCurrencySettingsKeyboard(long chatId, UserSettings userSettings) {
        sendMessage(chatId, "Виберіть валюту:", KeyboardFactory.getDynamicCurrencySettingsKeyboard(userSettings));
    }

    private void sendDecimalPlacesSettingsKeyboard(long chatId, UserSettings userSettings) {
        sendMessage(chatId, "Виберіть кількість знаків після коми:", KeyboardFactory.getDecimalPlacesSettingsKeyboard(userSettings));
    }

    private void sendNotificationsSettingsKeyboard(long chatId, UserSettings userSettings) {
        sendMessage(chatId, "Виберіть час сповіщення:", KeyboardFactory.getNotificationsSettingsKeyboard(userSettings));
    }

    private void updateBankSetting(long chatId, String bankName, int messageId, UserSettings userSettings) {
        Bank selectedBank = Bank.valueOf(bankName);
        List<Bank> banks = new ArrayList<>(userSettings.getChosenBanks());

        if (banks.contains(selectedBank)) {
            banks.remove(selectedBank);
            LOGGER.info("Removed bank: {}", selectedBank);
        } else {
            banks.add(selectedBank);
            LOGGER.info("Added bank: {}", selectedBank);
        }

        userSettings.setChosenBanks(banks);
        userSettingsProvider.setUserSettingsById(chatId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getDynamicBankSettingsKeyboard(userSettings));
    }

    private void updateCurrencySetting(long chatId, String currencyName, int messageId, UserSettings userSettings) {
        Currency selectedCurrency = Currency.valueOf(currencyName);
        List<Currency> currencies = new ArrayList<>(userSettings.getChosenCurrencies());

        if (currencies.contains(selectedCurrency)) {
            currencies.remove(selectedCurrency);
            LOGGER.info("Removed currency: {}", selectedCurrency);
        } else {
            currencies.add(selectedCurrency);
            LOGGER.info("Added currency: {}", selectedCurrency);
        }
        userSettings.setChosenCurrencies(currencies);
        userSettingsProvider.setUserSettingsById(chatId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getDynamicCurrencySettingsKeyboard(userSettings));
    }

    private void updateDecimalPlacesSetting(long chatId, int decimalPlaces, int messageId, UserSettings userSettings) {
        userSettings.setChosenCountSigns(decimalPlaces);
        LOGGER.info("Updated decimal places to: {}", decimalPlaces);
        userSettingsProvider.setUserSettingsById(chatId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getDecimalPlacesSettingsKeyboard(userSettings));
    }

    private void updateNotificationTime(long chatId, String time, int messageId, UserSettings userSettings) {
        Integer notificationTime = time != null ? Integer.parseInt(time.split(":")[0]) : null;
        userSettings.setTimeForNotify(notificationTime);
        LOGGER.info("Updated notification time to: {}", notificationTime != null ? notificationTime + ":00" : "Disabled");
        userSettingsProvider.setUserSettingsById(chatId, userSettings);
        updateKeyboard(chatId, messageId, KeyboardFactory.getNotificationsSettingsKeyboard(userSettings));
    }

    private void updateKeyboard(long chatId, int messageId, InlineKeyboardMarkup newKeyboard) {
        EditMessageReplyMarkup editMessageReplyMarkup = EditMessageReplyMarkup.builder()
                .chatId(chatId)
                .messageId(messageId)
                .replyMarkup(newKeyboard)
                .build();
        try {
            telegramClient.execute(editMessageReplyMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(long chatId, String text, InlineKeyboardMarkup replyMarkup) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .parseMode(ParseMode.HTML)
                .replyMarkup(replyMarkup)
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            LOGGER.error("Error sending message to chatId {}: {}", chatId, e.getMessage(), e);
        }
    }
}