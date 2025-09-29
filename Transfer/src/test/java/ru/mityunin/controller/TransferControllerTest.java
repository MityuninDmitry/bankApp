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

    @BeforeEach
    void setUp() {
        transferRequestDto = new TransferRequestDto();
        transferRequestDto.setLogin(testLogin);
        transferRequestDto.setAccountNumberFrom("1234567890");
        transferRequestDto.setAccountNumberTo("0987654321");
        transferRequestDto.setValue(new BigDecimal("1000.00"));
    }

    @Test
    void transferRequest_WhenBlockerServiceBlocks_ShouldReturnBadRequest() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.error("Operation blocked");
        when(blockerService.isBlockerOperation()).thenReturn(blockerResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Operation blocked")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Operation blocked", response.getBody().getMessage());

        verify(blockerService).isBlockerOperation();
        verify(notificationService).sendNotification(testLogin, "Operation blocked");
        verify(transferService, never()).transferOperation(any());
    }

    @Test
    void transferRequest_WhenTransferSuccess_ShouldReturnOk() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Transfer completed successfully");

        when(blockerService.isBlockerOperation()).thenReturn(blockerResponse);
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

        verify(blockerService).isBlockerOperation();
        verify(transferService).transferOperation(transferRequestDto);
        verify(notificationService).sendNotification(testLogin, "Transfer completed successfully");
    }

    @Test
    void transferRequest_WhenTransferFails_ShouldReturnBadRequest() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.error("Insufficient funds");

        when(blockerService.isBlockerOperation()).thenReturn(blockerResponse);
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

        verify(blockerService).isBlockerOperation();
        verify(transferService).transferOperation(transferRequestDto);
        verify(notificationService).sendNotification(testLogin, "Insufficient funds");
    }

    @Test
    void transferRequest_WhenNotificationServiceFails_ShouldStillReturnCorrectHttpStatus() {
        // Arrange
        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Transfer completed successfully");
        ApiResponse<Void> notificationResponse = ApiResponse.error("Failed to send notification");

        when(blockerService.isBlockerOperation()).thenReturn(blockerResponse);
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

        verify(blockerService).isBlockerOperation();
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
    void transferRequest_WithDifferentTransferScenarios_ShouldHandleAppropriately() {
        // Test with different amounts
        transferRequestDto.setValue(new BigDecimal("0.01"));

        ApiResponse<Void> blockerResponse = ApiResponse.success("Operation allowed");
        ApiResponse<Void> transferResponse = ApiResponse.success("Small transfer completed");

        when(blockerService.isBlockerOperation()).thenReturn(blockerResponse);
        when(transferService.transferOperation(transferRequestDto)).thenReturn(transferResponse);
        when(notificationService.sendNotification(eq(testLogin), eq("Small transfer completed")))
                .thenReturn(ApiResponse.success("Notification sent"));

        // Act
        ResponseEntity<ApiResponse<Void>> response = transferController.transferRequest(transferRequestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());

        verify(blockerService).isBlockerOperation();
        verify(transferService).transferOperation(transferRequestDto);
    }
}