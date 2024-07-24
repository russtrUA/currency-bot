package com.app.telegram.model;

import lombok.Getter;

@Getter
public enum Currency {
    EUR(978),
    GBP(826),
    USD(840);
    private final int code;

    Currency(int code) {
        this.code = code;
    }

    public static Currency getCurrencyByCode(int code) {
        for (Currency currency : Currency.values()) {
            if (currency.getCode() == code) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Unknown currency code: " + code);
    }

    public static boolean isValidCurrency(String currencyCode) {
        try {
            Currency.valueOf(currencyCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    public static boolean isValidCurrency(int currencyCode) {
        for (Currency currency : Currency.values()) {
            if (currency.getCode() == currencyCode) {
                return true;
            }
        }
        return false;
    }
}
