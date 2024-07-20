package com.app.telegram.features.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.app.telegram.constants.Constants.*;

public class KeyboardFactory {

    public static InlineKeyboardMarkup getMainKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow(RATES_BUTTON_NAME, "get_info"));
        keyboardRows.add(createRow(SETTINGS_BUTTON_NAME, "settings"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getSettingsKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow(BANKS_BUTTON_NAME, "bank"));
        keyboardRows.add(createRow(CURRENCIES_BUTTON_NAME, "currency"));
        keyboardRows.add(createRow(COUNT_SIGNS_BUTTON_NAME, "number_of_decimal_places"));
        keyboardRows.add(createRow(NOTIFICATION_BUTTON_NAME, "notifications"));
        keyboardRows.add(createRow(BACK_TO_MAIN_BUTTON, "back_to_menu"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getBankSettingsKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow("PrivatBank", "PrivatBank"));
        keyboardRows.add(createRow("Monobank", "Monobank"));
        keyboardRows.add(createRow("NBU", "NBU"));
        keyboardRows.add(createRow(BACK_BUTTON, "back"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getCurrencySettingsKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow("EUR", "EUR"));
        keyboardRows.add(createRow("USD", "USD"));
        keyboardRows.add(createRow(BACK_BUTTON, "back"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getDecimalPlacesSettingsKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow("2", "2"));
        keyboardRows.add(createRow("3", "3"));
        keyboardRows.add(createRow("4", "4"));
        keyboardRows.add(createRow(BACK_BUTTON, "back"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getNotificationsSettingsKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        for (int i = 9; i <= 18; i++) {
            String time = String.format("%02d:00", i);
            keyboardRows.add(createRow(time, time));
        }
        keyboardRows.add(createRow(OFF_NOTIFICATION_BUTTON_NAME, "Disable notifications"));
        keyboardRows.add(createRow(BACK_BUTTON, "back"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    private static InlineKeyboardRow createRow(String buttonText, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(callbackData)
                .build();
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);
        return row;
    }
}