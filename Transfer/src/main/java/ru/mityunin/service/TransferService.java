package ru.mityunin.service;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.dto.TransferRequest;
import ru.mityunin.model.CashOperation;
import ru.mityunin.model.CurrencyType;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransferService {
    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private final String accountsServiceUrl;
    private final String exchangeServiceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public TransferService(@Value("${service.accounts.url}") String accountsServiceUrl, @Value("${service.exchange.url}") String exchangeServiceUrl, RestTemplateHelper restTemplateHelper) {
        this.accountsServiceUrl = accountsServiceUrl;
        this.exchangeServiceUrl = exchangeServiceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    @Transactional
    public ApiResponse<Void> transferOperation(TransferRequest transferRequest) {
        log.info("[TransferService] transferOperation {}", transferRequest);
        // ссылки на эндпоинты
        String accountsUrl = accountsServiceUrl + "/accounts/processOperation";
        String accountInfoUrl = accountsServiceUrl + "/accounts/accountInfo";
        String exchangeUrl = exchangeServiceUrl + "/exchange/currencies";

        // выясняем тип валюты счетов откуда и куда перевод
        ApiResponse<PaymentAccountDto> accountInfoFrom = restTemplateHelper.postForApiResponse(accountInfoUrl, transferRequest.getAccountNumberFrom(), PaymentAccountDto.class);
        CurrencyType fromCurrency = accountInfoFrom.getData().getCurrency(); // валюта счета откуда перевод

        ApiResponse<PaymentAccountDto> accountInfoTo = restTemplateHelper.postForApiResponse(accountInfoUrl, transferRequest.getAccountNumberTo(), PaymentAccountDto.class);
        CurrencyType toCurrency = accountInfoTo.getData().getCurrency(); // валюта счета куда перевод

        // получем текущие курсы валют
        ApiResponse<ExchangeCurrencyDto[]> currenciesResponse = restTemplateHelper.getForApiResponse(exchangeUrl, ExchangeCurrencyDto[].class);

        BigDecimal money = transferRequest.getValue(); // валюта исходного счета
        ExchangeCurrencyDto exchangeCurrencyDto = getExchangeCurrencyDto(fromCurrency, CurrencyType.RUB, currenciesResponse);
        money = money.multiply(BigDecimal.valueOf(exchangeCurrencyDto.getValue())); // преобразование к рублям
        exchangeCurrencyDto = getExchangeCurrencyDto(CurrencyType.RUB, toCurrency, currenciesResponse);
        money = money.divide(BigDecimal.valueOf(exchangeCurrencyDto.getValue())); // преобразование в валюту счета перевода


        // операции снятия и внесения в валюте счета
        CashOperationRequest withdrawnOperation = new CashOperationRequest();
        withdrawnOperation.setMoney(transferRequest.getValue());
        withdrawnOperation.setAction(CashOperation.WITHDRAWN);
        withdrawnOperation.setAccountNumber(transferRequest.getAccountNumberFrom());
        log.info("[TransferService] transfer info. from {}, to {}, money from {}, money to {} ",fromCurrency,toCurrency,transferRequest.getValue(), money);
        // снимаем в валюте счета
        ApiResponse<Void> withdrawn = restTemplateHelper.postForApiResponse(accountsUrl, withdrawnOperation, Void.class);
        log.info("[TransferService] first transfer response WITHDRAWN {}",withdrawn);
        if (withdrawn.isSuccess()) { // если снятие успешно, то
            CashOperationRequest depositOperation = new CashOperationRequest();
            depositOperation.setMoney(money);
            depositOperation.setAction(CashOperation.DEPOSIT);
            depositOperation.setAccountNumber(transferRequest.getAccountNumberTo());

            // добавляем к другому счету
            ApiResponse<Void> deposit = restTemplateHelper.postForApiResponse(accountsUrl, depositOperation, Void.class);
            log.info("[TransferService] second transfer response DEPOSIT {}",deposit);
            if (deposit.isSuccess()) { // если успех, то успех. На выход
                return ApiResponse.success("Успех перевода деняк");
            } else {
                // возврат денег
                withdrawnOperation = new CashOperationRequest();
                withdrawnOperation.setMoney(transferRequest.getValue());
                withdrawnOperation.setAction(CashOperation.DEPOSIT);
                withdrawnOperation.setAccountNumber(transferRequest.getAccountNumberFrom());

                // снимаем в валюте счета
                restTemplateHelper.postForApiResponse(accountsUrl, withdrawnOperation, Void.class);

                return ApiResponse.error(deposit.getMessage());
            }
        } else {
            return ApiResponse.error(withdrawn.getMessage());
        }

    }

    public ExchangeCurrencyDto getExchangeCurrencyDto(CurrencyType from, CurrencyType to, ApiResponse<ExchangeCurrencyDto[]> response) {
        for(ExchangeCurrencyDto exchangeCurrencyDto: response.getData()) {
            if (exchangeCurrencyDto.getCurrencyFrom().equals(from) && exchangeCurrencyDto.getCurrencyTo().equals(to)) {
                return exchangeCurrencyDto;
            }
        }
        return null;
    }
}
