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

        StringBuilder result = new StringBuilder(MONEY_WITH_WINGS).append("<b>Поточні курси валют:</b>\n");

        for (Bank bank : chosenBanks) {
            result.append("\n <b>").append(BANK_EMOJI).append(bank).append("</b>:\n");
            List<BankRateDto> ratesForBank = bankRateDtoList.stream()
                    .filter(rate -> rate.getBank() == bank && chosenCurrencies.contains(rate.getCurrency()))
                    .toList();
            if (ratesForBank.isEmpty())
                if (bankResponseStatuses.get(bank) != 200)
                    result.append("\n" + WARNING_EMOJI + "Технічні проблеми на стороні банку.\nСпробуйте через 10 хвилин."
                            + HOURGLASS_EMOJI + "\n");
                else
                    result.append("\n" + MAGNIFYING_GLASS_EMOJI + "Для даного банку курсів\n " +
                            "по вибраних валютах не знайдено.\n");
            for (Currency currency : chosenCurrencies) {
                for (BankRateDto rate : ratesForBank) {
                    if (rate.getCurrency() == currency) {
                        result.append("\n" + "<b>").append(currency).append(" => UAH").append("</b>\n");
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