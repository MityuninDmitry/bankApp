package ru.mityunin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.ExchangeCurrencyDto;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Service
public class ExchangeService {

    private final String serviceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public ExchangeService(@Value("${service.exchangegenerator.url}") String serviceUrl, RestTemplateHelper restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<List<ExchangeCurrencyDto>> actualCurrencies() {
        String url = serviceUrl + "/exchangegenerator/currencies";
        ApiResponse<ExchangeCurrencyDto[]> response = restTemplateHelper.getForApiResponse(url, ExchangeCurrencyDto[].class);
        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? Arrays.asList(response.getData()) : null
        );
    }
}
