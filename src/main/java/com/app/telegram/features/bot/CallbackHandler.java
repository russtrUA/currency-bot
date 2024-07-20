package com.app.telegram.features.bot;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class CallbackHandler {
    private final TelegramClient telegramClient;

    public CallbackHandler(TelegramClient telegramClient) {
        this.telegramClient = telegramClient;
    }

    public void handleCallback(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        long chat_id = update.getCallbackQuery().getMessage().getChatId();
        long user_id = update.getCallbackQuery().getFrom().getId();

        switch (callbackData) {
            case "settings":
                sendSettingsKeyboard(chat_id);
                break;
            case "back_to_menu":
                sendMainKeyboard(chat_id, "How can I help you?");
                break;
            case "get_info":
                System.out.println("Here will be info from CurrencyRateProvider");
                break;
            case "bank":
                sendBankSettingsKeyboard(chat_id);
                break;
            case "currency":
                sendCurrencySettingsKeyboard(chat_id);
                break;
            case "number_of_decimal_places":
                sendDecimalPlacesSettingsKeyboard(chat_id);
                break;
            case "notifications":
                sendNotificationsSettingsKeyboard(chat_id);
                break;
            case "back":
                sendSettingsKeyboard(chat_id);
                break;
            default:
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

    private void sendBankSettingsKeyboard(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose a bank:")
                .replyMarkup(KeyboardFactory.getBankSettingsKeyboard())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendCurrencySettingsKeyboard(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose a currency:")
                .replyMarkup(KeyboardFactory.getCurrencySettingsKeyboard())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendDecimalPlacesSettingsKeyboard(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose the number of decimal places:")
                .replyMarkup(KeyboardFactory.getDecimalPlacesSettingsKeyboard())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendNotificationsSettingsKeyboard(long chatId) {
        SendMessage message = SendMessage.builder()
                .chatId(chatId)
                .text("Choose notification time:")
                .replyMarkup(KeyboardFactory.getNotificationsSettingsKeyboard())
                .build();
        try {
            telegramClient.execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}