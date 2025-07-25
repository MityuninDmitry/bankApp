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

import java.io.DataInput;

@Service
public class CashService {
    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    private final String accountsServiceUrl;
    private final String blockerServiceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public CashService(
            @Value("${service.accounts.url}") String accountsServiceUrl,
            @Value("${service.blocker.url}") String blockerServiceUrl,
            RestTemplateHelper restTemplateHelper) {
        this.accountsServiceUrl = accountsServiceUrl;
        this.blockerServiceUrl = blockerServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> processOperation(CashOperationRequest cashOperationRequest) {
        log.info("cash operation {}", cashOperationRequest);
        String accountsUrl = accountsServiceUrl + "/accounts/processOperation";
        String blockerUrl = blockerServiceUrl + "/blocker/checkOperation";
        ApiResponse<Void> suspiciousOperationResponse = restTemplateHelper.postForApiResponse(blockerUrl, cashOperationRequest, Void.class);
        if (suspiciousOperationResponse.isSuccess()) {
            return restTemplateHelper.postForApiResponse(accountsUrl,cashOperationRequest, Void.class);
        } else  {
            return suspiciousOperationResponse;
        }

    }
}
