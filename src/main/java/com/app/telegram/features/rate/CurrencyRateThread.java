package com.app.telegram.features.rate;

import com.app.telegram.features.rate.dto.BankRateDto;
import com.app.telegram.features.rate.dto.responses.MonoBankRateResponseDto;
import com.app.telegram.features.rate.dto.responses.NbuRateResponseDto;
import com.app.telegram.features.rate.dto.responses.PryvatBankRateResponseDto;
import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.app.telegram.model.Currency.getCurrencyByCode;
import static com.app.telegram.model.Currency.isValidCurrency;

public class CurrencyRateThread extends Thread {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private List<PryvatBankRateResponseDto> pryvatBankRateResponseDtoList;
    private List<MonoBankRateResponseDto> monobankRateResponseDtoList;
    private List<NbuRateResponseDto> nbuRateResponseDtoList;


    @SneakyThrows
    @Override
    public void run() {
        CurrencyRateProvider.getInstance().setBankRateDtoList(aggregateBankRates());
    }

    public List<BankRateDto> aggregateBankRates() throws IOException, InterruptedException {
        initializeBankRateLists();
        List<BankRateDto> aggregatedRates = new ArrayList<>();
        aggregatedRates.addAll(mapPryvatBankRates(pryvatBankRateResponseDtoList));
        aggregatedRates.addAll(mapMonoBankRates(monobankRateResponseDtoList));
        aggregatedRates.addAll(mapNbuRates(nbuRateResponseDtoList));
        return aggregatedRates;
    }

    private List<BankRateDto> mapPryvatBankRates(List<PryvatBankRateResponseDto> pryvatBankRates) {
        return pryvatBankRates.stream()
                .filter(rate -> isValidCurrency(rate.getCcy()) && "UAH".equals(rate.getBase_ccy()))
                .map(rate -> BankRateDto.builder()
                        .bank(Bank.Pryvatbank)
                        .currency(Currency.valueOf(rate.getCcy()))
                        .saleRate(Double.valueOf(rate.getSale()))
                        .buyRate(Double.valueOf(rate.getBuy()))
                        .build())
                .collect(Collectors.toList());
    }

    private List<BankRateDto> mapMonoBankRates(List<MonoBankRateResponseDto> monobankRates) {
        return monobankRates.stream()
                .filter(rate -> isValidCurrency(rate.getCurrencyCodeA()) && rate.getCurrencyCodeB() == 980)
                .map(rate -> BankRateDto.builder()
                        .bank(Bank.Monobank)
                        .currency(getCurrencyByCode(rate.getCurrencyCodeA()))
                        .saleRate(rate.getRateSell())
                        .buyRate(rate.getRateBuy())
                        .middleRate(rate.getRateCross())
                        .build())
                .collect(Collectors.toList());
    }

    private List<BankRateDto> mapNbuRates(List<NbuRateResponseDto> nbuRates) {
        return nbuRates.stream()
                .filter(rate -> isValidCurrency(rate.getR030()))
                .map(rate -> BankRateDto.builder()
                        .bank(Bank.NBU)
                        .currency(getCurrencyByCode(rate.getR030()))
                        .middleRate(rate.getRate())
                        .build())
                .collect(Collectors.toList());
    }

    private void initializeBankRateLists() throws IOException, InterruptedException {
        pryvatBankRateResponseDtoList = getBankRates(Bank.Pryvatbank, new TypeReference<>() {
        });
        monobankRateResponseDtoList = getBankRates(Bank.Monobank, new TypeReference<>() {
        });
        nbuRateResponseDtoList = getBankRates(Bank.NBU, new TypeReference<>() {
        });
    }

    private <T> T getBankRates(Bank bank, TypeReference<T> typeReference) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(bank.getApiUrl()))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), typeReference);
    }
}








