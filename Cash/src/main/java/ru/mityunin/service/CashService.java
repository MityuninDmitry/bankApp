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

    public ApiResponse<Void> processOperation(CashOperationRequestDto cashOperationRequestDto) {
        log.info("cash operation {}", cashOperationRequestDto);
        String accountsUrl = accountsServiceUrl + "/accounts/processOperation";
        String blockerUrl = blockerServiceUrl + "/blocker/checkOperation";
        ApiResponse<Void> suspiciousOperationResponse = restTemplateHelper.postForApiResponse(blockerUrl, cashOperationRequestDto, Void.class);
        if (suspiciousOperationResponse.isSuccess()) {
            return restTemplateHelper.postForApiResponse(accountsUrl, cashOperationRequestDto, Void.class);
        } else  {
            return suspiciousOperationResponse;
        }

    }
}
