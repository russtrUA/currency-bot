package com.app.telegram.features.rate;

import com.app.telegram.features.rate.dto.BankRateDto;
import com.app.telegram.features.rate.dto.responses.MonoBankRateResponseDto;
import com.app.telegram.features.rate.dto.responses.NbuRateResponseDto;
import com.app.telegram.features.rate.dto.responses.PryvatBankRateResponseDto;
import com.app.telegram.model.Bank;
import com.app.telegram.model.Currency;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.app.telegram.model.Currency.getCurrencyByCode;
import static com.app.telegram.model.Currency.isValidCurrency;

public class CurrencyRateThread extends Thread {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Bank, Integer> bankResponseStatuses = CurrencyRateProvider.getInstance().getBankResponseStatuses();
    private static final Logger LOGGER = LoggerFactory.getLogger(CurrencyRateThread.class);

    @Override
    public void run() {
        CurrencyRateProvider.getInstance().setBankRateDtoList(aggregateBankRates());
    }

    private List<BankRateDto> aggregateBankRates() {
        List<MonoBankRateResponseDto> monobankRateResponseDtoList = getBankRates(Bank.Monobank, new TypeReference<>() {
        });
        List<PryvatBankRateResponseDto> pryvatBankRateResponseDtoList = getBankRates(Bank.Pryvatbank, new TypeReference<>() {
        });
        List<NbuRateResponseDto> nbuRateResponseDtoList = getBankRates(Bank.NBU, new TypeReference<>() {
        });
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

    private <T> T getBankRates(Bank bank, TypeReference<T> typeReference) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(bank.getApiUrl()))
                .GET()
                .build();
        T emptyResult = null;
        try {
            emptyResult = objectMapper.readValue("[]", typeReference);
        } catch (JsonProcessingException e) {
            LOGGER.error("An error occurred while processing empty list json: {}", e.getMessage(), e);
        }
        try {
            HttpResponse<String>response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            bankResponseStatuses.put(bank, response.statusCode());
            LOGGER.info("Request to bank : {}", bank.name());
            LOGGER.info("Response status code : {}", response.statusCode());
            if (response.statusCode() != 200) {
                return emptyResult;
            }
            return objectMapper.readValue(response.body(), typeReference);
        } catch (IOException | InterruptedException e) {
            LOGGER.error("An error occurred while processing the request: {}", e.getMessage(), e);
        }
        return emptyResult;
    }
}