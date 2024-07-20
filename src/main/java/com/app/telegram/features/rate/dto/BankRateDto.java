package com.app.telegram.features.rate.dto;

import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import lombok.*;

@Data
@Builder
public class BankRateDto {
    private Bank bank;
    private Currency currency;
    private Double saleRate;
    private Double buyRate;
    private Double middleRate;
}
