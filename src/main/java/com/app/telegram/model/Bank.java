package com.app.telegram.model;

public enum Bank {
    Pryvatbank() {
        @Override
        public String getApiUrl() {
            return "https://api.privatbank.ua/p24api/pubinfo?exchange&json&coursid=11";
        }
    },
    Monobank {
        @Override
        public String getApiUrl() {
            return "https://api.monobank.ua/bank/currency";
        }
    },
    NBU {
        @Override
        public String getApiUrl() {
            return "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?json";
        }
    };
    @Override
    public String toString() {
        return switch (this) {
            case Pryvatbank -> "Приватбанк";
            case Monobank -> "Монобанк";
            case NBU -> "Національний Банк України";
        };
    }
    public abstract String getApiUrl();
}