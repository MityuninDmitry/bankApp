package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.CashOperationRequestDto;

@Service
public class CashService {
    private static final Logger log = LoggerFactory.getLogger(CashService.class);
    private final String accountsServiceUrl;
    private final String apiAccounts;
    private final AuthenticatedRestTemplateService restTemplateHelper;

    public CashService(
            @Value("${service.url.gateway}") String accountsServiceUrl,
            @Value("${service.api.accounts}") String apiAccounts,
            AuthenticatedRestTemplateService restTemplateHelper) {
        this.accountsServiceUrl = accountsServiceUrl;
        this.apiAccounts = apiAccounts;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> processOperation(CashOperationRequestDto cashOperationRequestDto) {
        log.info("cash operation {}", cashOperationRequestDto);
        String accountsUrl = accountsServiceUrl + apiAccounts + "/api/processOperation";
        return restTemplateHelper.postForApiResponse(accountsUrl, cashOperationRequestDto, Void.class);
    }
}
