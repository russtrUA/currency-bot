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
}
