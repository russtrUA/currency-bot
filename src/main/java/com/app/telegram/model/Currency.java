package com.app.telegram.model;
import lombok.Getter;

@Getter
public enum Currency {
    EUR(978),
    USD(840);
    private final int code;
    Currency(int code) {
        this.code = code;
    }

    public static boolean isValidCurrency(String currencyCode) {
        try {
            Currency.valueOf(currencyCode);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
