package ru.mityunin.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.dto.TransferRequestDto;
import ru.mityunin.model.CashOperation;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final String accountsServiceUrl;
    @Value("${service.api.accounts}")
    private String apiAccounts;
    private final AuthenticatedRestTemplateService restTemplateHelper;

    public TransferService(
            @Value("${service.url.gateway}") String accountsServiceUrl,
            AuthenticatedRestTemplateService restTemplateHelper) {
        this.accountsServiceUrl = accountsServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    @Transactional
    public ApiResponse<Void> transferOperation(TransferRequestDto transferRequestDto) {
        log.info("[TransferService] transferOperation {}", transferRequestDto);
        // ссылки на эндпоинты
        String accountsUrl = accountsServiceUrl + apiAccounts + "/api/processOperation";

        // операции снятия и внесения в валюте счета
        CashOperationRequest withdrawnOperationRequest = new CashOperationRequest();
        withdrawnOperationRequest.setMoney(transferRequestDto.getValue());
        withdrawnOperationRequest.setAction(CashOperation.WITHDRAWN);
        withdrawnOperationRequest.setAccountNumber(transferRequestDto.getAccountNumberFrom());
        // снимаем в валюте счета
        ApiResponse<Void> withdrawn = restTemplateHelper.postForApiResponse(accountsUrl, withdrawnOperationRequest, Void.class);
        log.info("[TransferService] first transfer response WITHDRAWN {}",withdrawn);
        if (withdrawn.isSuccess()) { // если снятие успешно, то
            CashOperationRequest depositOperation = new CashOperationRequest();
            depositOperation.setMoney(transferRequestDto.getValue());
            depositOperation.setAction(CashOperation.DEPOSIT);
            depositOperation.setAccountNumber(transferRequestDto.getAccountNumberTo());

            // добавляем к другому счету
            ApiResponse<Void> deposit = restTemplateHelper.postForApiResponse(accountsUrl, depositOperation, Void.class);
            log.info("[TransferService] second transfer response DEPOSIT {}",deposit);
            if (deposit.isSuccess()) { // если успех, то успех. На выход
                return ApiResponse.success("Успех перевода деняк");
            } else {
                // возврат денег
                withdrawnOperationRequest = new CashOperationRequest();
                withdrawnOperationRequest.setMoney(transferRequestDto.getValue());
                withdrawnOperationRequest.setAction(CashOperation.DEPOSIT);
                withdrawnOperationRequest.setAccountNumber(transferRequestDto.getAccountNumberFrom());

                // снимаем в валюте счета
                restTemplateHelper.postForApiResponse(accountsUrl, withdrawnOperationRequest, Void.class);

                return ApiResponse.error(deposit.getMessage());
            }
        } else {
            return ApiResponse.error(withdrawn.getMessage());
        }

    }
}
