package ru.mityunin.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;


import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.CashOperationRequest;

@Service
public class CashService {
    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    private final String serviceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public CashService(
            @Value("${service.cash.url}") String serviceUrl,
            RestTemplateHelper restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse processOperation(CashOperationRequest cashOperationRequest) {
        log.info("Cash operation request {}", cashOperationRequest);
        String url = serviceUrl + "/cash/processOperation";
        return restTemplateHelper.postForApiResponse(url, cashOperationRequest, Void.class);
    }
}
