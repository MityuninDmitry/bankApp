package ru.mityunin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.dto.ExchangeCurrencyFrontUIDto;
import ru.mityunin.model.CurrencyType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class ExchangeService {

    private final String serviceUrl;
    private final AuthenticatedRestTemplateService restTemplateHelper;

    public ExchangeService(@Value("${service.url.gateway}") String serviceUrl, AuthenticatedRestTemplateService restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<List<ExchangeCurrencyDto>> actualCurrencies() {
        String url = serviceUrl + "/exchangegenerator/api/currencies";
        ApiResponse<ExchangeCurrencyDto[]> response = restTemplateHelper.getForApiResponse(url, ExchangeCurrencyDto[].class);
        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? Arrays.asList(response.getData()) : null
        );
    }

    public ApiResponse<List<ExchangeCurrencyFrontUIDto>> actualCurrenciesFrontUI() {
        String url = serviceUrl + "/exchangegenerator/api/currencies";
        ApiResponse<ExchangeCurrencyDto[]> response = restTemplateHelper.getForApiResponse(url, ExchangeCurrencyDto[].class);
        List<ExchangeCurrencyFrontUIDto> exchangeCurrencyFrontUIDtoList = new ArrayList<>();
        HashMap<String, ExchangeCurrencyFrontUIDto> hashMap = new HashMap<>();
        for (CurrencyType currencyType: CurrencyType.values()) {
            if (!currencyType.equals(CurrencyType.RUB)) {
                ExchangeCurrencyFrontUIDto exchangeCurrencyFrontUIDto = new ExchangeCurrencyFrontUIDto();
                exchangeCurrencyFrontUIDto.setCurrency(currencyType);
                exchangeCurrencyFrontUIDto.setSellPrice(0);
                exchangeCurrencyFrontUIDto.setBuyPrice(0);
                hashMap.put(String.valueOf(currencyType),exchangeCurrencyFrontUIDto);
            }
        }

        for (ExchangeCurrencyDto exchangeCurrencyDto: response.getData()) {
            if(exchangeCurrencyDto.getCurrencyFrom().equals(CurrencyType.RUB) && !exchangeCurrencyDto.getCurrencyTo().equals(CurrencyType.RUB)) {
                hashMap.get(exchangeCurrencyDto.getCurrencyTo().toString()).setBuyPrice(exchangeCurrencyDto.getValue());
            }
            if (exchangeCurrencyDto.getCurrencyTo().equals(CurrencyType.RUB) && !exchangeCurrencyDto.getCurrencyFrom().equals(CurrencyType.RUB)) {
                hashMap.get(exchangeCurrencyDto.getCurrencyFrom().toString()).setSellPrice(exchangeCurrencyDto.getValue());
            }
        }

        exchangeCurrencyFrontUIDtoList.addAll(hashMap.values());

        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? exchangeCurrencyFrontUIDtoList : null
        );
    }


}
