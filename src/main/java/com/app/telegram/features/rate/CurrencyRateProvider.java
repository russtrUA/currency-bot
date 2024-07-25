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
import static com.app.telegram.constants.Constants.MONEY_WITH_WINGS;


public class CurrencyRateProvider {

    private static CurrencyRateProvider currencyRateProvider;
    @Getter
    @Setter
    private List<BankRateDto> bankRateDtoList;

    private CurrencyRateProvider() {

    }

    public static CurrencyRateProvider getInstance() {
        if (currencyRateProvider == null) {
            currencyRateProvider = new CurrencyRateProvider();
        }
        return currencyRateProvider;
    }

    public String getPrettyRatesByChatId(long chatId) {

        UserSettings userSettings = UserSettingsProvider.getInstance().getUserSettingsById(chatId);

        List<Bank> chosenBanks = userSettings.getChosenBanks();
        List<Currency> chosenCurrencies = userSettings.getChosenCurrencies();
        int chosenCountSigns = userSettings.getChosenCountSigns();

        StringBuilder result = new StringBuilder(MONEY_WITH_WINGS).append("<b>Поточні курси валют:</b>\n");

        for (Bank bank : chosenBanks) {
            result.append("\n <b>").append(BANK_EMOJI).append(bank).append("</b>\n");
            List<BankRateDto> ratesForBank = bankRateDtoList.stream()
                    .filter(rate -> rate.getBank() == bank && chosenCurrencies.contains(rate.getCurrency()))
                    .toList();

            for (Currency currency : chosenCurrencies) {
                for (BankRateDto rate : ratesForBank) {
                    if (rate.getCurrency() == currency) {
                        result.append("<b>").append(currency).append("=>UAH").append("</b>\n");
                        if (rate.getMiddleRate() != null) {
                            result.append("  Курс: ").append(formatRate(rate.getMiddleRate(), chosenCountSigns)).append("\n");
                        }
                        if (rate.getBuyRate() != null) {
                            result.append("  Купівля: ").append(formatRate(rate.getBuyRate(), chosenCountSigns)).append("\n");
                        }
                        if (rate.getSaleRate() != null) {
                            result.append("  Продаж: ").append(formatRate(rate.getSaleRate(), chosenCountSigns)).append("\n");
                        }
                    }
                }
            }
        }
        return result.toString();
    }

    private String formatRate(Double rate, int signs) {
        if (rate == null) {
            return "N/A";
        }
        return String.format("%." + signs + "f", rate);
    }
}


