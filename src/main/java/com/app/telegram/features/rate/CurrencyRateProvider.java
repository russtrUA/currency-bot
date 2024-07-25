package com.app.telegram.features.rate;

import com.app.telegram.features.rate.dto.BankRateDto;
import com.app.telegram.features.user.UserSettings;
import com.app.telegram.features.user.UserSettingsProvider;
import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import static com.app.telegram.constants.Constants.BANK_EMOJI;

/**
 * Клас, що надає курси валют для користувачів бота Telegram.
 */
@Setter
@Getter
public class CurrencyRateProvider {

    private static CurrencyRateProvider currencyRateProvider;
    private List<BankRateDto> bankRateDtoList;

    /**
     * Приватний конструктор для запобігання створення екземплярів класу.
     */
    private CurrencyRateProvider() {

    }

    /**
     * Повертає екземпляр класу `CurrencyRateProvider`.
     *
     * @return екземпляр класу `CurrencyRateProvider`
     */
    public static CurrencyRateProvider getInstance() {
        if (currencyRateProvider == null) {
            currencyRateProvider = new CurrencyRateProvider();
        }
        return currencyRateProvider;
    }

    /**
     * Повертає відформатований рядок з курсами валют для заданого користувача.
     *
     * @param chatId ID чату користувача
     * @return відформатований рядок з курсами валют
     */
    public String getPrettyRatesByChatId(long chatId) {
        UserSettings userSettings = UserSettingsProvider.getInstance().getUserSettingsById(chatId);

        List<Bank> chosenBanks = userSettings.getChosenBanks();
        List<Currency> chosenCurrencies = userSettings.getChosenCurrencies();
        int chosenCountSigns = userSettings.getChosenCountSigns();

        StringBuilder result = new StringBuilder("Курси валют:\n");

        for (Bank bank : chosenBanks) {
            result.append(BANK_EMOJI).append(bank).append("\n");
            List<BankRateDto> ratesForBank = bankRateDtoList.stream()
                    .filter(rate -> rate.getBank() == bank && chosenCurrencies.contains(rate.getCurrency()))
                    .toList();

            for (Currency currency : chosenCurrencies) {
                for (BankRateDto rate : ratesForBank) {
                    if (rate.getCurrency() == currency) {
                        if (rate.getMiddleRate() != null) {
                            result.append(currency).append("\n")
                                    .append("  Курс: ").append(formatRate(rate.getMiddleRate(), chosenCountSigns)).append("\n");
                        }
                        if (rate.getBuyRate() != null) {
                            result.append(currency).append("\n")
                                    .append("  Купівля: ").append(formatRate(rate.getBuyRate(), chosenCountSigns)).append("\n");
                        }
                        if (rate.getSaleRate() != null) {
                            result.append("  Продаж: ").append(formatRate(rate.getSaleRate(), chosenCountSigns)).append("\n");
                        }
                    }
                }
            }
            result.append("\n");
        }
        return result.toString();
    }

    /**
     * Форматує курс валют з заданою кількістю знаків після коми.
     *
     * @param rate курс валют
     * @param signs кількість знаків після коми
     * @return відформатований курс валют
     */
    private String formatRate(Double rate, int signs) {
        if (rate == null) {
            return "N/A";
        }
        return String.format("%." + signs + "f", rate);
    }
}
