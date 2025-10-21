package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.model.CashOperation;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CashServiceTest {

    @Mock
    private AuthenticatedRestTemplateService restTemplateHelper;

    private CashService cashService;

    private CashOperationRequestDto cashOperationRequest;

    @BeforeEach
    void setUp() {
        // Mock values from application.properties
        String accountsServiceUrl = "http://localhost:8080";
        String blockerServiceUrl = "http://localhost:8080";
        String apiAccounts = "/accounts";
        String apiBlocker = "/blocker";

        cashService = new CashService(
                accountsServiceUrl,
                blockerServiceUrl,
                restTemplateHelper
        );

        cashOperationRequest = new CashOperationRequestDto();
        cashOperationRequest.setAccountNumber("1234567890");
        cashOperationRequest.setAction(CashOperation.DEPOSIT);
        cashOperationRequest.setMoney(new BigDecimal("100.50"));
        cashOperationRequest.setLogin("testUser");
    }

    @Test
    void processOperation_WhenBlockerApproves_ShouldCallAccountsService() {
        // Arrange
        ApiResponse<Void> blockerSuccessResponse = ApiResponse.success("Operation approved");
        ApiResponse<Void> accountsSuccessResponse = ApiResponse.success("Money deposited successfully");

        when(restTemplateHelper.postForApiResponse(
                eq("http://localhost:8080/blocker/api/checkOperation"),
                eq(cashOperationRequest),
                eq(Void.class)))
                .thenReturn(blockerSuccessResponse);

        when(restTemplateHelper.postForApiResponse(
                eq("http://localhost:8080/accounts/api/processOperation"),
                eq(cashOperationRequest),
                eq(Void.class)))
                .thenReturn(accountsSuccessResponse);

        // Act
        ApiResponse<Void> result = cashService.processOperation(cashOperationRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Money deposited successfully", result.getMessage());

        verify(restTemplateHelper, times(1))
                .postForApiResponse("http://localhost:8080/blocker/api/checkOperation", cashOperationRequest, Void.class);
        verify(restTemplateHelper, times(1))
                .postForApiResponse("http://localhost:8080/accounts/api/processOperation", cashOperationRequest, Void.class);
    }

    @Test
    void processOperation_WhenBlockerRejects_ShouldNotCallAccountsService() {
        // Arrange
        ApiResponse<Void> blockerErrorResponse = ApiResponse.error("Suspicious operation detected");

        when(restTemplateHelper.postForApiResponse(
                eq("http://localhost:8080/blocker/api/checkOperation"),
                eq(cashOperationRequest),
                eq(Void.class)))
                .thenReturn(blockerErrorResponse);

        // Act
        ApiResponse<Void> result = cashService.processOperation(cashOperationRequest);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Suspicious operation detected", result.getMessage());

        verify(restTemplateHelper, times(1))
                .postForApiResponse("http://localhost:8080/blocker/api/checkOperation", cashOperationRequest, Void.class);
        verify(restTemplateHelper, never())
                .postForApiResponse("http://localhost:8080/accounts/api/processOperation", cashOperationRequest, Void.class);
    }

    @Test
    void processOperation_WhenWithdrawOperation_ShouldProcessCorrectly() {
        // Arrange
        cashOperationRequest.setAction(CashOperation.WITHDRAWN);
        cashOperationRequest.setMoney(new BigDecimal("50.00"));

        ApiResponse<Void> blockerSuccessResponse = ApiResponse.success("Operation approved");
        ApiResponse<Void> accountsSuccessResponse = ApiResponse.success("Money withdrawn successfully");

        when(restTemplateHelper.postForApiResponse(
                anyString(),
                eq(cashOperationRequest),
                eq(Void.class)))
                .thenReturn(blockerSuccessResponse)
                .thenReturn(accountsSuccessResponse);

        // Act
        ApiResponse<Void> result = cashService.processOperation(cashOperationRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Money withdrawn successfully", result.getMessage());
    }

    @Test
    void processOperation_WhenZeroAmount_ShouldProcessCorrectly() {
        // Arrange
        cashOperationRequest.setMoney(BigDecimal.ZERO);

        ApiResponse<Void> blockerSuccessResponse = ApiResponse.success("Operation approved");
        ApiResponse<Void> accountsSuccessResponse = ApiResponse.success("Operation completed");

        when(restTemplateHelper.postForApiResponse(anyString(), any(), eq(Void.class)))
                .thenReturn(blockerSuccessResponse)
                .thenReturn(accountsSuccessResponse);

        // Act
        ApiResponse<Void> result = cashService.processOperation(cashOperationRequest);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
    }
}