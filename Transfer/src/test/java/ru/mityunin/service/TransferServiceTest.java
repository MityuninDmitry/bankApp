package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequest;
import ru.mityunin.dto.ExchangeCurrencyDto;
import ru.mityunin.dto.PaymentAccountDto;
import ru.mityunin.dto.TransferRequestDto;
import ru.mityunin.model.CashOperation;
import ru.mityunin.model.CurrencyType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AuthenticatedRestTemplateService restTemplateHelper;

    private TransferService transferService;

    private final String gatewayUrl = "http://localhost:8080";
    private final String apiAccountsPath = "/accounts";
    private final String apiExchangePath = "/exchange";

    private TransferRequestDto transferRequestDto;
    private final String accountFrom = "1234567890";
    private final String accountTo = "0987654321";
    private final BigDecimal transferAmount = new BigDecimal("1000.00");

    @BeforeEach
    void setUp() {
        transferService = new TransferService(gatewayUrl, gatewayUrl, restTemplateHelper);

        // Устанавливаем значения через рефлексию
        setFieldViaReflection("apiAccounts", apiAccountsPath);
        setFieldViaReflection("apiExchange", apiExchangePath);

        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setAccountNumberFrom(accountFrom);
        transferRequestDto.setAccountNumberTo(accountTo);
        transferRequestDto.setValue(transferAmount);
        transferRequestDto.setLogin("testUser");
    }

    private void setFieldViaReflection(String fieldName, String value) {
        try {
            var field = TransferService.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(transferService, value);
        } catch (Exception e) {
            fail("Failed to set " + fieldName + " field via reflection", e);
        }
    }

    private PaymentAccountDto createPaymentAccount(CurrencyType currency) {
        PaymentAccountDto account = new PaymentAccountDto();
        account.setCurrency(currency);
        account.setBalance(BigDecimal.valueOf(5000.00));
        return account;
    }

    private ExchangeCurrencyDto[] createExchangeRates() {
        return new ExchangeCurrencyDto[] {
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.USD, CurrencyType.RUB, 1),
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.RUB, CurrencyType.USD, 90),
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.CNY, CurrencyType.RUB, 1),
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.RUB, CurrencyType.CNY, 100),
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.USD, CurrencyType.CNY, 4),
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.CNY, CurrencyType.USD, 10)
        };
    }

    @Test
    void transferOperation_WhenSameCurrency_ShouldTransferSuccessfully() {
        // Arrange
        String accountsUrl = gatewayUrl + apiAccountsPath + "/api/processOperation";
        String accountInfoUrl = gatewayUrl + apiAccountsPath + "/api/accountInfo";
        String exchangeUrl = gatewayUrl + apiExchangePath + "/api/currencies";

        // Оба счета в USD
        ApiResponse<PaymentAccountDto> accountFromResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<PaymentAccountDto> accountToResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<ExchangeCurrencyDto[]> exchangeRates = ApiResponse.success("Success", createExchangeRates());

        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountFrom), eq(PaymentAccountDto.class)))
                .thenReturn(accountFromResponse);
        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountTo), eq(PaymentAccountDto.class)))
                .thenReturn(accountToResponse);
        when(restTemplateHelper.getForApiResponse(eq(exchangeUrl), eq(ExchangeCurrencyDto[].class)))
                .thenReturn(exchangeRates);

        // Успешные операции снятия и пополнения
        ApiResponse<Void> withdrawnResponse = ApiResponse.success("Withdrawn successful");
        ApiResponse<Void> depositResponse = ApiResponse.success("Deposit successful");

        when(restTemplateHelper.postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class)))
                .thenReturn(withdrawnResponse)  // Первый вызов - снятие
                .thenReturn(depositResponse);   // Второй вызов - пополнение

        // Act
        ApiResponse<Void> result = transferService.transferOperation(transferRequestDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Успех перевода деняк", result.getMessage());

        verify(restTemplateHelper, times(2)).postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class));
    }

    @Test
    void transferOperation_WhenDifferentCurrencies_ShouldConvertAndTransfer() {
        // Arrange
        String accountsUrl = gatewayUrl + apiAccountsPath + "/api/processOperation";
        String accountInfoUrl = gatewayUrl + apiAccountsPath + "/api/accountInfo";
        String exchangeUrl = gatewayUrl + apiExchangePath + "/api/currencies";

        // Счета в разных валютах
        ApiResponse<PaymentAccountDto> accountFromResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<PaymentAccountDto> accountToResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.CNY));
        ApiResponse<ExchangeCurrencyDto[]> exchangeRates = ApiResponse.success("Success", createExchangeRates());

        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountFrom), eq(PaymentAccountDto.class)))
                .thenReturn(accountFromResponse);
        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountTo), eq(PaymentAccountDto.class)))
                .thenReturn(accountToResponse);
        when(restTemplateHelper.getForApiResponse(eq(exchangeUrl), eq(ExchangeCurrencyDto[].class)))
                .thenReturn(exchangeRates);

        ApiResponse<Void> withdrawnResponse = ApiResponse.success("Withdrawn successful");
        ApiResponse<Void> depositResponse = ApiResponse.success("Deposit successful");

        when(restTemplateHelper.postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class)))
                .thenReturn(withdrawnResponse)
                .thenReturn(depositResponse);

        // Act
        ApiResponse<Void> result = transferService.transferOperation(transferRequestDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());

        verify(restTemplateHelper, times(2)).postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class));
    }

    @Test
    void transferOperation_WhenWithdrawalFails_ShouldReturnError() {
        // Arrange
        String accountsUrl = gatewayUrl + apiAccountsPath + "/api/processOperation";
        String accountInfoUrl = gatewayUrl + apiAccountsPath + "/api/accountInfo";
        String exchangeUrl = gatewayUrl + apiExchangePath + "/api/currencies";

        ApiResponse<PaymentAccountDto> accountFromResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<PaymentAccountDto> accountToResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<ExchangeCurrencyDto[]> exchangeRates = ApiResponse.success("Success", createExchangeRates());

        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountFrom), eq(PaymentAccountDto.class)))
                .thenReturn(accountFromResponse);
        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountTo), eq(PaymentAccountDto.class)))
                .thenReturn(accountToResponse);
        when(restTemplateHelper.getForApiResponse(eq(exchangeUrl), eq(ExchangeCurrencyDto[].class)))
                .thenReturn(exchangeRates);

        ApiResponse<Void> withdrawnResponse = ApiResponse.error("Insufficient funds");

        when(restTemplateHelper.postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class)))
                .thenReturn(withdrawnResponse);

        // Act
        ApiResponse<Void> result = transferService.transferOperation(transferRequestDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Insufficient funds", result.getMessage());

        verify(restTemplateHelper, times(1)).postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class));
    }

    @Test
    void transferOperation_WhenDepositFails_ShouldRollback() {
        // Arrange
        String accountsUrl = gatewayUrl + apiAccountsPath + "/api/processOperation";
        String accountInfoUrl = gatewayUrl + apiAccountsPath + "/api/accountInfo";
        String exchangeUrl = gatewayUrl + apiExchangePath + "/api/currencies";

        ApiResponse<PaymentAccountDto> accountFromResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<PaymentAccountDto> accountToResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<ExchangeCurrencyDto[]> exchangeRates = ApiResponse.success("Success", createExchangeRates());

        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountFrom), eq(PaymentAccountDto.class)))
                .thenReturn(accountFromResponse);
        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountTo), eq(PaymentAccountDto.class)))
                .thenReturn(accountToResponse);
        when(restTemplateHelper.getForApiResponse(eq(exchangeUrl), eq(ExchangeCurrencyDto[].class)))
                .thenReturn(exchangeRates);

        ApiResponse<Void> withdrawnResponse = ApiResponse.success("Withdrawn successful");
        ApiResponse<Void> depositResponse = ApiResponse.error("Account not found");
        ApiResponse<Void> rollbackResponse = ApiResponse.success("Rollback successful");

        when(restTemplateHelper.postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class)))
                .thenReturn(withdrawnResponse)  // Первый вызов - успешное снятие
                .thenReturn(depositResponse)    // Второй вызов - ошибка пополнения
                .thenReturn(rollbackResponse);  // Третий вызов - возврат средств

        // Act
        ApiResponse<Void> result = transferService.transferOperation(transferRequestDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Account not found", result.getMessage());

        // Проверяем, что было 3 вызова: снятие, пополнение (ошибка), возврат
        verify(restTemplateHelper, times(3)).postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class));
    }


    @Test
    void transferOperation_WhenExchangeRateNotFound_ShouldReturnNullPointer() {
        // Arrange
        String accountInfoUrl = gatewayUrl + apiAccountsPath + "/api/accountInfo";
        String exchangeUrl = gatewayUrl + apiExchangePath + "/api/currencies";

        ApiResponse<PaymentAccountDto> accountFromResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<PaymentAccountDto> accountToResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.CNY));

        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountFrom), eq(PaymentAccountDto.class)))
                .thenReturn(accountFromResponse);
        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountTo), eq(PaymentAccountDto.class)))
                .thenReturn(accountToResponse);

        // Курсы без нужной пары
        ExchangeCurrencyDto[] incompleteRates = new ExchangeCurrencyDto[] {
                new ExchangeCurrencyDto(LocalDateTime.now(), CurrencyType.USD, CurrencyType.CNY, 1)
        };
        ApiResponse<ExchangeCurrencyDto[]> exchangeResponse = ApiResponse.success("Success", incompleteRates);

        when(restTemplateHelper.getForApiResponse(eq(exchangeUrl), eq(ExchangeCurrencyDto[].class)))
                .thenReturn(exchangeResponse);

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            transferService.transferOperation(transferRequestDto);
        });
    }

    @Test
    void getExchangeCurrencyDto_WhenRateExists_ShouldReturnCorrectRate() {
        // Arrange
        ExchangeCurrencyDto[] rates = createExchangeRates();
        ApiResponse<ExchangeCurrencyDto[]> response = ApiResponse.success("Success", rates);

        // Act
        ExchangeCurrencyDto result = transferService.getExchangeCurrencyDto(CurrencyType.USD, CurrencyType.RUB, response);

        // Assert
        assertNotNull(result);
        assertEquals(CurrencyType.USD, result.getCurrencyFrom());
        assertEquals(CurrencyType.RUB, result.getCurrencyTo());
        assertEquals(1, result.getValue());
    }

    @Test
    void getExchangeCurrencyDto_WhenRateNotFound_ShouldReturnNull() {
        // Arrange
        ExchangeCurrencyDto[] rates = createExchangeRates();
        ApiResponse<ExchangeCurrencyDto[]> response = ApiResponse.success("Success", rates);

        // Act
        ExchangeCurrencyDto result = transferService.getExchangeCurrencyDto(CurrencyType.RUB, CurrencyType.RUB, response);

        // Assert
        assertNull(result);
    }

    @Test
    void transferOperation_WithSmallAmount_ShouldHandlePrecisionCorrectly() {
        // Arrange
        String accountsUrl = gatewayUrl + apiAccountsPath + "/api/processOperation";
        String accountInfoUrl = gatewayUrl + apiAccountsPath + "/api/accountInfo";
        String exchangeUrl = gatewayUrl + apiExchangePath + "/api/currencies";

        transferRequestDto.setValue(new BigDecimal("0.01"));

        ApiResponse<PaymentAccountDto> accountFromResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<PaymentAccountDto> accountToResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.CNY));
        ApiResponse<ExchangeCurrencyDto[]> exchangeRates = ApiResponse.success("Success", createExchangeRates());

        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountFrom), eq(PaymentAccountDto.class)))
                .thenReturn(accountFromResponse);
        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountTo), eq(PaymentAccountDto.class)))
                .thenReturn(accountToResponse);
        when(restTemplateHelper.getForApiResponse(eq(exchangeUrl), eq(ExchangeCurrencyDto[].class)))
                .thenReturn(exchangeRates);

        ApiResponse<Void> withdrawnResponse = ApiResponse.success("Withdrawn successful");
        ApiResponse<Void> depositResponse = ApiResponse.success("Deposit successful");

        when(restTemplateHelper.postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class)))
                .thenReturn(withdrawnResponse)
                .thenReturn(depositResponse);

        // Act
        ApiResponse<Void> result = transferService.transferOperation(transferRequestDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }

    @Test
    void transferOperation_WhenRollbackFails_ShouldStillReturnOriginalError() {
        // Arrange
        String accountsUrl = gatewayUrl + apiAccountsPath + "/api/processOperation";
        String accountInfoUrl = gatewayUrl + apiAccountsPath + "/api/accountInfo";
        String exchangeUrl = gatewayUrl + apiExchangePath + "/api/currencies";

        ApiResponse<PaymentAccountDto> accountFromResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<PaymentAccountDto> accountToResponse = ApiResponse.success("Success", createPaymentAccount(CurrencyType.USD));
        ApiResponse<ExchangeCurrencyDto[]> exchangeRates = ApiResponse.success("Success", createExchangeRates());

        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountFrom), eq(PaymentAccountDto.class)))
                .thenReturn(accountFromResponse);
        when(restTemplateHelper.postForApiResponse(eq(accountInfoUrl), eq(accountTo), eq(PaymentAccountDto.class)))
                .thenReturn(accountToResponse);
        when(restTemplateHelper.getForApiResponse(eq(exchangeUrl), eq(ExchangeCurrencyDto[].class)))
                .thenReturn(exchangeRates);

        ApiResponse<Void> withdrawnResponse = ApiResponse.success("Withdrawn successful");
        ApiResponse<Void> depositResponse = ApiResponse.error("Deposit failed");
        ApiResponse<Void> rollbackErrorResponse = ApiResponse.error("Rollback failed");

        when(restTemplateHelper.postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class)))
                .thenReturn(withdrawnResponse)
                .thenReturn(depositResponse)
                .thenReturn(rollbackErrorResponse);

        // Act
        ApiResponse<Void> result = transferService.transferOperation(transferRequestDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        // Должен вернуть оригинальную ошибку депозита, а не ошибку роллбэка
        assertEquals("Deposit failed", result.getMessage());

        verify(restTemplateHelper, times(3)).postForApiResponse(eq(accountsUrl), any(CashOperationRequest.class), eq(Void.class));
    }
}