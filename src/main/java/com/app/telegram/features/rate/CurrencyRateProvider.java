package com.app.telegram.features.rate;

import com.app.telegram.features.rate.dto.BankRateDto;
import com.app.telegram.features.user.UserSettings;
import com.app.telegram.features.user.UserSettingsProvider;
import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.app.telegram.constants.Constants.*;


@Setter
@Getter
public class CurrencyRateProvider {

    private static CurrencyRateProvider currencyRateProvider;
    private List<BankRateDto> bankRateDtoList;
    private final Map<Bank, Integer> bankResponseStatuses = new HashMap<>();

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

        StringBuilder result = new StringBuilder(MONEY_WITH_WINGS).append("<i><b>Поточні курси валют:</b></i>\n");

        for (Bank bank : chosenBanks) {
            result.append("\n <i><b>").append(BANK_EMOJI).append(bank).append("</b></i>:\n").append("\n");
            List<BankRateDto> ratesForBank = bankRateDtoList.stream()
                    .filter(rate -> rate.getBank() == bank && chosenCurrencies.contains(rate.getCurrency()))
                    .toList();
            if (ratesForBank.isEmpty())
                if (bankResponseStatuses.get(bank) != 200)
                    result.append("<i>" + WARNING_EMOJI + "Технічні проблеми на стороні банку.\nСпробуйте через 10 хвилин."
                            + HOURGLASS_EMOJI + "</i>\n");
                else
                    result.append("<i>" + MAGNIFYING_GLASS_EMOJI + "Для даного банку курсів\n " +
                            "по вибраних валютах не знайдено.</i>\n");
            for (Currency currency : chosenCurrencies) {
                for (BankRateDto rate : ratesForBank) {
                    if (rate.getCurrency() == currency) {
                        result.append("<i><b>").append(currency).append(" " + CONVERSION_ICON + " UAH").append("</b></i>\n");
                        if (rate.getMiddleRate() != null) {
                            result.append("  <i><u>Курс: ").append(formatRate(rate.getMiddleRate(), chosenCountSigns)).append("</u></i>\n");
                        }
                        if (rate.getBuyRate() != null) {
                            result.append("  <i><u>Купівля: ").append(formatRate(rate.getBuyRate(), chosenCountSigns)).append("</u></i>\n");
                        }
                        if (rate.getSaleRate() != null) {
                            result.append("  <i><u>Продаж: ").append(formatRate(rate.getSaleRate(), chosenCountSigns)).append("</u></i>\n");
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
        String pattern = "%." + signs + "f";
        return String.format(pattern, rate);
    }
}