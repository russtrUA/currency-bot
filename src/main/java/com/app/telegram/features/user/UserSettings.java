package com.app.telegram.features.user;

import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import lombok.*;

import java.util.*;

@Data
@Builder
@AllArgsConstructor
@Getter
public class UserSettings {
    private List<Bank> chosenBanks;
    private Integer chosenCountSigns;
    private List<Currency> chosenCurrencies;
    private Integer timeForNotify;

    public UserSettings() {
        List<Bank> defaultBankList = new ArrayList<>();
        defaultBankList.add(Bank.Pryvatbank);
        this.chosenBanks = defaultBankList;
        List<Currency> defaultCurrencyList = new ArrayList<>();
        defaultCurrencyList.add(Currency.USD);
        this.chosenCurrencies = defaultCurrencyList;
        this.chosenCountSigns = 2;
    }
}
