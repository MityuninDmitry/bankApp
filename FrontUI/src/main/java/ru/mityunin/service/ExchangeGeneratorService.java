package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.dto.ExchangeCurrency;

import java.util.Arrays;
import java.util.List;

@Service
public class ExchangeGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    private final String serviceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public ExchangeGeneratorService(@Value("${service.exchangegenerator.url}") String serviceUrl, RestTemplateHelper restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<List<ExchangeCurrency>> currencies() {
        String url = serviceUrl + "/exchangegenerator/currencies";
        ApiResponse<ExchangeCurrency[]> response = restTemplateHelper.getForApiResponse(url, ExchangeCurrency[].class);
        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? Arrays.asList(response.getData()) : null
        );
    }

}
