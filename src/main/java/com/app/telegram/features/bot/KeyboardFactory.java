package com.app.telegram.features.bot;

import com.app.telegram.features.user.UserSettings;
import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
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

    public static InlineKeyboardMarkup getBankSettingsKeyboard(UserSettings userSettings) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRowWithCheck("Приватбанк", "Pryvatbank", userSettings.getChosenBanks().contains(Bank.Pryvatbank)));
        keyboardRows.add(createRowWithCheck("Монобанк", "Monobank", userSettings.getChosenBanks().contains(Bank.Monobank)));
        keyboardRows.add(createRowWithCheck("Національний Банк України", "NBU", userSettings.getChosenBanks().contains(Bank.NBU)));
        keyboardRows.add(createRow(BACK_BUTTON, "back"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getCurrencySettingsKeyboard(UserSettings userSettings) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRowWithCheck("EUR", "EUR", userSettings.getChosenCurrencies().contains(Currency.EUR)));
        keyboardRows.add(createRowWithCheck("USD", "USD", userSettings.getChosenCurrencies().contains(Currency.USD)));
        keyboardRows.add(createRow(BACK_BUTTON, "back"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getDecimalPlacesSettingsKeyboard(UserSettings userSettings) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRowWithCheck("2", "2", userSettings.getChosenCountSigns() == 2));
        keyboardRows.add(createRowWithCheck("3", "3", userSettings.getChosenCountSigns() == 3));
        keyboardRows.add(createRowWithCheck("4", "4", userSettings.getChosenCountSigns() == 4));
        keyboardRows.add(createRow(BACK_BUTTON, "back"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    public static InlineKeyboardMarkup getNotificationsSettingsKeyboard(UserSettings userSettings) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (int i = 9; i <= 18; i++) {
            String time = String.format("%02d:00", i);
            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(userSettings.getTimeForNotify() != null && userSettings.getTimeForNotify() == i ? CHECKED_ITEM + time : time)
                    .callbackData(time)
                    .build();
            currentRow.add(button);

            if (currentRow.size() == 2) {
                keyboardRows.add(new InlineKeyboardRow(currentRow));
                currentRow = new ArrayList<>();
            }
        }

        if (!currentRow.isEmpty()) {
            keyboardRows.add(new InlineKeyboardRow(currentRow));
        }

        keyboardRows.add(createRowWithCheck(OFF_NOTIFICATION_BUTTON_NAME, "Disable notifications", userSettings.getTimeForNotify() == null));
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

    private static InlineKeyboardRow createRowWithCheck(String buttonText, String callbackData, boolean isSelected) {
        String text = isSelected ? CHECKED_ITEM + buttonText : buttonText;
        return createRow(text, callbackData);
    }
}