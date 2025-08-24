package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.dto.ExchangeCurrencyFrontUIDto;

import java.util.Arrays;
import java.util.List;

@Service
public class ExchangeService {

    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    private final String serviceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public ExchangeService(@Value("${service.url.exchange}") String serviceUrl, RestTemplateHelper restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<List<ExchangeCurrencyFrontUIDto>> currencies() {
        String url = serviceUrl + "/api/currenciesFrontUI";
        ApiResponse<ExchangeCurrencyFrontUIDto[]> response = restTemplateHelper.getForApiResponse(url, ExchangeCurrencyFrontUIDto[].class);
        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? Arrays.asList(response.getData()) : null
        );
    }

}
