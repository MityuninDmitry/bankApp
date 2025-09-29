package ru.mityunin.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.TransferRequestDto;
import ru.mityunin.service.BlockerService;
import ru.mityunin.service.NotificationService;
import ru.mityunin.service.TransferService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferControllerTest {

    @Mock
    private TransferService transferService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private BlockerService blockerService;

    @InjectMocks
    private TransferController transferController;

    private TransferRequestDto transferRequestDto;
    private final String testLogin = "testUser";
    private final BigDecimal transferAmount = new BigDecimal("1000.00");

    @BeforeEach
    void setUp() {
        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setLogin(testLogin);
        transferRequestDto.setAccountNumberFrom("1234567890");
        transferRequestDto.setAccountNumberTo("0987654321");
        transferRequestDto.setValue(transferAmount);
    }

    @Test
    void transferRequest_WhenBlockerServiceBlocks_ShouldReturnBadRequest() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.error("Operation blocked");
        when(blockerService.isBlockerOperation(transferAmount)).thenReturn(blockerResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Operation blocked")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Operation blocked", response.getBody().getMessage());

        verify(blockerService).isBlockerOperation(transferAmount);
        verify(notificationService).sendNotification(testLogin, "Operation blocked");
        verify(transferService, never()).transferOperation(any());
    }

    @Test
    void transferRequest_WhenTransferSuccess_ShouldReturnOk() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Transfer completed successfully");

        when(blockerService.isBlockerOperation(transferAmount)).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Transfer completed successfully")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Transfer completed successfully", response.getBody().getMessage());

        verify(blockerService).isBlockerOperation(transferAmount);
        verify(transferService).transferOperation(transferRequestDto);
        verify(notificationService).sendNotification(testLogin, "Transfer completed successfully");
    }

    @Test
    void transferRequest_WhenTransferFails_ShouldReturnBadRequest() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.error("Insufficient funds");

        when(blockerService.isBlockerOperation(transferAmount)).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Insufficient funds")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Insufficient funds", response.getBody().getMessage());

        verify(blockerService).isBlockerOperation(transferAmount);
        verify(transferService).transferOperation(transferRequestDto);
        verify(notificationService).sendNotification(testLogin, "Insufficient funds");
    }

    @Test
    void transferRequest_WhenNotificationServiceFails_ShouldStillReturnCorrectHttpStatus() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Transfer completed successfully");
        ApiResponse<Void> notificationResponse = ApiResponse.error("Failed to send notification");

        when(blockerService.isBlockerOperation(transferAmount)).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Transfer completed successfully")))
                .thenReturn(notificationResponse);

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        // Проверяем, что даже если нотификация не отправилась, основной ответ все равно успешный
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Transfer completed successfully", response.getBody().getMessage());

        verify(blockerService).isBlockerOperation(transferAmount);
        verify(transferService).transferOperation(transferRequestDto);
        verify(notificationService).sendNotification(testLogin, "Transfer completed successfully");
    }

    @Test
    void transferRequest_WithNullRequestBody_ShouldHandleGracefully() {
        // Arrange
        TransferRequestDto nullRequest = null;

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            transferController.transferRequest(nullRequest);
        });
    }

    @Test
    void transferRequest_WithDifferentAmounts_ShouldPassCorrectAmountToBlocker() {
        // Test with different amounts
        BigDecimal smallAmount = new BigDecimal("0.01");
        transferRequestDto.setValue(smallAmount);

        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Small transfer completed");

        when(blockerService.isBlockerOperation(smallAmount)).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Small transfer completed")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(blockerService).isBlockerOperation(smallAmount);
        verify(transferService).transferOperation(transferRequestDto);
    }

    @Test
    void transferRequest_WithLargeAmount_ShouldPassLargeAmountToBlocker() {
        // Arrange
        BigDecimal largeAmount = new BigDecimal("1000000.00");
        transferRequestDto.setValue(largeAmount);

        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Large transfer completed");

        when(blockerService.isBlockerOperation(largeAmount)).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Large transfer completed")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(blockerService).isBlockerOperation(largeAmount);
        verify(transferService).transferOperation(transferRequestDto);
    }

    @Test
    void transferRequest_WhenBlockerServiceThrowsException_ShouldPropagateException() {
        // Arrange
        when(blockerService.isBlockerOperation(transferAmount))
                .thenThrow(new RuntimeException("Blocker service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            transferController.transferRequest(transferRequestDto);
        });

        assertEquals("Blocker service unavailable", exception.getMessage());
        verify(blockerService).isBlockerOperation(transferAmount);
        verify(transferService, never()).transferOperation(any());
        verify(notificationService, never()).sendNotification(any(), any());
    }

    @Test
    void transferRequest_WithZeroAmount_ShouldPassZeroToBlocker() {
        // Arrange
        BigDecimal zeroAmount = BigDecimal.ZERO;
        transferRequestDto.setValue(zeroAmount);

        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Zero transfer completed");

        when(blockerService.isBlockerOperation(zeroAmount)).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Zero transfer completed")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(blockerService).isBlockerOperation(zeroAmount);
        verify(transferService).transferOperation(transferRequestDto);
    }

    @Test
    void transferRequest_WithNegativeAmount_ShouldPassNegativeToBlocker() {
        // Arrange
        BigDecimal negativeAmount = new BigDecimal("-100.00");
        transferRequestDto.setValue(negativeAmount);

        ApiResponse<Void> blockerResponse = ApiResponse.error("Negative amount not allowed");
        when(blockerService.isBlockerOperation(negativeAmount)).thenReturn(blockerResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Negative amount not allowed")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());

        verify(blockerService).isBlockerOperation(negativeAmount);
        verify(transferService, never()).transferOperation(any());
    }

    @Test
    void transferRequest_ShouldUseExactAmountFromRequest() {
        // Arrange
        BigDecimal exactAmount = new BigDecimal("1234.5678");
        transferRequestDto.setValue(exactAmount);

        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Exact transfer completed");

        when(blockerService.isBlockerOperation(exactAmount)).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Exact transfer completed")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Проверяем, что передается именно то значение, которое пришло в запросе
        verify(blockerService).isBlockerOperation(exactAmount);
    }
}