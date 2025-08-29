package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.CashOperationRequestDto;

@Service
public class CashService {
    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    private final String serviceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public CashService(
            @Value("${service.url.gateway}") String serviceUrl,
            RestTemplateHelper restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> processOperation(CashOperationRequestDto cashOperationRequestDto) {
        log.info("Cash operation request {}", cashOperationRequestDto);
        String url = serviceUrl + "/cash/api/processOperation";
        return restTemplateHelper.postForApiResponse(url, cashOperationRequestDto, Void.class);
    }
}
