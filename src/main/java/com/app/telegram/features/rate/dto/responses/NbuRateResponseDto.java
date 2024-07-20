package com.app.telegram.features.rate.dto.responses;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NbuRateResponseDto {
    private int r030;
    private String txt;
    private Double rate;
    private String cc;
    private String exchangedate;
}
