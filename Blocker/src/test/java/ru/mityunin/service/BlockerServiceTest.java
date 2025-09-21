package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.CashOperationRequestDto;
import ru.mityunin.model.CashOperation;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class BlockerServiceTest {

    private BlockerService blockerService;

    @BeforeEach
    void setUp() {
        blockerService = new BlockerService();
    }

    @Test
    void isSuspiciousOperation_WithDeposit_ShouldReturnSuccess() {
        // Arrange
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setAction(CashOperation.DEPOSIT);
        request.setMoney(new BigDecimal("100.00"));
        request.setLogin("testUser");
        request.setAccountNumber("123456");

        // Act
        ApiResponse<Void> result = blockerService.isSuspiciousOperation(request);

        // Assert
        assertTrue(result.isSuccess());
        assertEquals("Все отлично. Работаем.", result.getMessage());
    }

    @Test
    void isSuspiciousOperation_WithWithdrawal_ShouldReturnEitherSuccessOrError() {
        // Arrange
        CashOperationRequestDto request = new CashOperationRequestDto();
        request.setAction(CashOperation.WITHDRAWN);
        request.setMoney(new BigDecimal("100.00"));
        request.setLogin("testUser");
        request.setAccountNumber("123456");

        // Act
        ApiResponse<Void> result = blockerService.isSuspiciousOperation(request);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessage());

        // Т,к рандом
        if (result.isSuccess()) {
            assertEquals("Все отлично. Работаем.", result.getMessage());
        } else {
            assertFalse(result.isSuccess());
            assertEquals("Подозрительная операция. Отклонено!", result.getMessage());
        }
    }


    @Test
    void isSuspiciousOperation_WithoutParams_ShouldReturnEitherSuccessOrError() {
        // Act
        ApiResponse<Void> result = blockerService.isSuspiciousOperation(BigDecimal.valueOf(10000));

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMessage());

        if (result.isSuccess()) {
            assertEquals("Все отлично. Работаем.", result.getMessage());
        } else {
            assertFalse(result.isSuccess());
            assertEquals("Подозрительная операция. Отклонено!", result.getMessage());
        }
    }
}