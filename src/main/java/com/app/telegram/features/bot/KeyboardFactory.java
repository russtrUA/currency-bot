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

/**
 * Клас для створення клавіатур для Telegram бота.
 */
public class KeyboardFactory {

    /**
     * Створює головну клавіатуру.
     *
     * @return головна клавіатура
     */
    public static InlineKeyboardMarkup getMainKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow(RATES_BUTTON_NAME, "get_info"));
        keyboardRows.add(createRow(SETTINGS_BUTTON_NAME, "settings"));
        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    /**
     * Створює клавіатуру налаштувань.
     *
     * @return клавіатура налаштувань
     */
    public static InlineKeyboardMarkup getSettingsKeyboard() {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRow(BANKS_BUTTON_NAME, "bank"));
        keyboardRows.add(createRow(CURRENCIES_BUTTON_NAME, "currency"));
        keyboardRows.add(createRow(COUNT_SIGNS_BUTTON_NAME, "number_of_decimal_places"));
        keyboardRows.add(createRow(NOTIFICATION_BUTTON_NAME, "notifications"));

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(BACK_TO_MAIN_BUTTON).callbackData("back_to_menu").build());
        row.add(InlineKeyboardButton.builder().text(RATES_BUTTON_NAME).callbackData("get_info").build());
        keyboardRows.add(new InlineKeyboardRow(row));

        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    /**
     * Створює клавіатуру налаштувань кількості знаків після коми.
     *
     * @param userSettings налаштування користувача
     * @return клавіатура налаштувань кількості знаків після коми
     */
    public static InlineKeyboardMarkup getDecimalPlacesSettingsKeyboard(UserSettings userSettings) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        keyboardRows.add(createRowWithCheck("2", "2", userSettings.getChosenCountSigns() == 2));
        keyboardRows.add(createRowWithCheck("3", "3", userSettings.getChosenCountSigns() == 3));
        keyboardRows.add(createRowWithCheck("4", "4", userSettings.getChosenCountSigns() == 4));
        getKeyboardButtons(keyboardRows);

        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    /**
     * Створює клавіатуру налаштувань часу сповіщень.
     *
     * @param userSettings налаштування користувача
     * @return клавіатура налаштувань часу сповіщень
     */
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
        getKeyboardButtons(keyboardRows);

        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    /**
     * Створює динамічну клавіатуру налаштувань банків.
     *
     * @param userSettings налаштування користувача
     * @return динамічна клавіатура налаштувань банків
     */
    public static InlineKeyboardMarkup getDynamicBankSettingsKeyboard(UserSettings userSettings) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        for (Bank bank : Bank.values()) {
            boolean isSelected = userSettings.getChosenBanks().contains(bank);
            keyboardRows.add(createRowWithCheck(bank.toString(), bank.name(), isSelected));
        }
        getKeyboardButtons(keyboardRows);

        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    /**
     * Створює динамічну клавіатуру налаштувань валют.
     *
     * @param userSettings налаштування користувача
     * @return динамічна клавіатура налаштувань валют
     */
    public static InlineKeyboardMarkup getDynamicCurrencySettingsKeyboard(UserSettings userSettings) {
        List<InlineKeyboardRow> keyboardRows = new ArrayList<>();
        for (Currency currency : Currency.values()) {
            boolean isSelected = userSettings.getChosenCurrencies().contains(currency);
            keyboardRows.add(createRowWithCheck(currency.name(), currency.name(), isSelected));
        }
        getKeyboardButtons(keyboardRows);

        return InlineKeyboardMarkup.builder().keyboard(keyboardRows).build();
    }

    /**
     * Додає кнопки "Назад" і "Отримати інформацію" до клавіатури.
     *
     * @param keyboardRows список рядків клавіатури
     */
    private static void getKeyboardButtons(List<InlineKeyboardRow> keyboardRows) {
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(InlineKeyboardButton.builder().text(BACK_BUTTON).callbackData("back").build());
        row.add(InlineKeyboardButton.builder().text(RATES_BUTTON_NAME).callbackData("get_info").build());
        keyboardRows.add(new InlineKeyboardRow(row));
    }

    /**
     * Створює рядок клавіатури з однією кнопкою.
     *
     * @param buttonText текст кнопки
     * @param callbackData callback-дані для кнопки
     * @return рядок клавіатури
     */
    private static InlineKeyboardRow createRow(String buttonText, String callbackData) {
        InlineKeyboardButton button = InlineKeyboardButton.builder()
                .text(buttonText)
                .callbackData(callbackData)
                .build();
        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);
        return row;
    }

    /**
     * Створює рядок клавіатури з однією кнопкою та позначкою вибору.
     *
     * @param buttonText текст кнопки
     * @param callbackData callback-дані для кнопки
     * @param isSelected чи вибрана кнопка
     * @return рядок клавіатури
     */
    private static InlineKeyboardRow createRowWithCheck(String buttonText, String callbackData, boolean isSelected) {
        String text = isSelected ? CHECKED_ITEM + buttonText : buttonText;
        return createRow(text, callbackData);
    }
}
