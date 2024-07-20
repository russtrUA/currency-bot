package com.app.telegram.features.user;

import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import lombok.*;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
public class UserSettings {
    private List<Bank> chosenBanks;
    private Integer chosenCountSigns;
    private List<Currency> chosenCurrencies;
    private Integer timeForNotify;
    public UserSettings() {
        List<Bank> defaultBankList = new ArrayList<>();
        defaultBankList.add(Bank.Pryvatbank);
        chosenBanks = defaultBankList;
        List<Currency> defaultCurrencyList = new ArrayList<>();
        defaultCurrencyList.add(Currency.USD);
        chosenCurrencies = defaultCurrencyList;
        chosenCountSigns = 2;
    }
}
