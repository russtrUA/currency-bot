package com.app.telegram.features.rate.dto.responses;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PryvatBankRateResponseDto {
    private String ccy;
    private String base_ccy;
    private String buy;
    private String sale;
}
