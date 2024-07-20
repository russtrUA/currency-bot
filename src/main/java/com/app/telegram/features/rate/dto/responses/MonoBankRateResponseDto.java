package com.app.telegram.features.rate.dto.responses;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonoBankRateResponseDto {
    private int currencyCodeA;
    private int currencyCodeB;
    private long date;
    private Double rateBuy;
    private Double rateSell;
    private Double rateCross;
}
