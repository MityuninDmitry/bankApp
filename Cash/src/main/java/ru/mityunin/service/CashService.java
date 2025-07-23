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
    private final RestTemplateHelper restTemplateHelper;

    public CashService(@Value("${service.accounts.url}") String accountsServiceUrl, RestTemplateHelper restTemplateHelper) {
        this.accountsServiceUrl = accountsServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> processOperation(CashOperationRequest cashOperationRequest) {
        log.info("cash operation {}", cashOperationRequest);
        String url = accountsServiceUrl + "/accounts/processOperation";
        return restTemplateHelper.postForApiResponse(url,cashOperationRequest, Void.class);
    }
}
