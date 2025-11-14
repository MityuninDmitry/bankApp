package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
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

    private CashOperationRequestDto requestDto;

    @BeforeEach
    void setUp() {
        cashService = new CashService("http://localhost:8080", restTemplateHelper);
        ReflectionTestUtils.setField(cashService, "apiCash", "/cash-service");

        requestDto = new CashOperationRequestDto();
        requestDto.setAccountNumber("1234567890");
        requestDto.setAction(CashOperation.DEPOSIT);
        requestDto.setMoney(new BigDecimal("100.50"));
        requestDto.setLogin("testuser");
    }

    @Test
    void processOperation_Success() {
        // Arrange
        ApiResponse<Void> expectedResponse = ApiResponse.success("Operation processed");
        when(restTemplateHelper.postForApiResponse(anyString(), any(CashOperationRequestDto.class), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> result = cashService.processOperation(requestDto);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertEquals("Operation processed", result.getMessage());
        verify(restTemplateHelper).postForApiResponse(
                "http://localhost:8080/cash-service/api/processOperation",
                requestDto,
                Void.class
        );
    }

    @Test
    void processOperation_Error() {
        // Arrange
        ApiResponse<Void> expectedResponse = ApiResponse.error("Service unavailable");
        when(restTemplateHelper.postForApiResponse(anyString(), any(CashOperationRequestDto.class), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> result = cashService.processOperation(requestDto);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Service unavailable", result.getMessage());
    }

    @Test
    void processOperation_Exception() {
        // Arrange
        when(restTemplateHelper.postForApiResponse(anyString(), any(CashOperationRequestDto.class), eq(Void.class)))
                .thenThrow(new RuntimeException("Network error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            cashService.processOperation(requestDto);
        });
    }
}