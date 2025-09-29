package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockerServiceTest {

    @Mock
    private AuthenticatedRestTemplateService restTemplateHelper;

    private BlockerService blockerService;

    private final String gatewayUrl = "http://localhost:8080";
    private final String apiBlockerPath = "/blocker";

    @BeforeEach
    void setUp() {
        blockerService = new BlockerService(gatewayUrl, restTemplateHelper);

        // Устанавливаем значение apiBlocker через рефлексию
        try {
            var field = BlockerService.class.getDeclaredField("apiBlocker");
            field.setAccessible(true);
            field.set(blockerService, apiBlockerPath);
        } catch (Exception e) {
            fail("Failed to set apiBlocker field via reflection", e);
        }
    }

    @Test
    void isBlockerOperation_WhenServiceReturnsSuccess_ShouldReturnSuccessResponse() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("Operation allowed");
        BigDecimal amount = new BigDecimal("1000.00");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(amount), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> actualResponse = blockerService.isBlockerOperation(amount);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Operation allowed", actualResponse.getMessage());

        verify(restTemplateHelper).postForApiResponse(expectedUrl, amount, Void.class);
    }

    @Test
    void isBlockerOperation_WhenServiceReturnsError_ShouldReturnErrorResponse() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.error("Operation blocked");
        BigDecimal amount = new BigDecimal("5000.00");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(amount), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> actualResponse = blockerService.isBlockerOperation(amount);

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isSuccess());
        assertEquals("Operation blocked", actualResponse.getMessage());

        verify(restTemplateHelper).postForApiResponse(expectedUrl, amount, Void.class);
    }

    @Test
    void isBlockerOperation_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        BigDecimal amount = new BigDecimal("10000.00");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(amount), eq(Void.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            blockerService.isBlockerOperation(amount);
        });

        assertEquals("Service unavailable", exception.getMessage());
        verify(restTemplateHelper).postForApiResponse(expectedUrl, amount, Void.class);
    }

    @Test
    void isBlockerOperation_WithDifferentAmounts_ShouldPassCorrectAmount() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");

        // Test with different amounts
        BigDecimal smallAmount = new BigDecimal("10.50");
        BigDecimal largeAmount = new BigDecimal("100000.00");
        BigDecimal zeroAmount = BigDecimal.ZERO;
        BigDecimal negativeAmount = new BigDecimal("-100.00");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), any(BigDecimal.class), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act & Assert for small amount
        blockerService.isBlockerOperation(smallAmount);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, smallAmount, Void.class);

        // Act & Assert for large amount
        blockerService.isBlockerOperation(largeAmount);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, largeAmount, Void.class);

        // Act & Assert for zero amount
        blockerService.isBlockerOperation(zeroAmount);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, zeroAmount, Void.class);

        // Act & Assert for negative amount
        blockerService.isBlockerOperation(negativeAmount);
        verify(restTemplateHelper, times(4)).postForApiResponse(eq(expectedUrl), any(BigDecimal.class), eq(Void.class));
    }

    @Test
    void isBlockerOperation_WithNullAmount_ShouldPassNull() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), isNull(), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = blockerService.isBlockerOperation(null);

        // Assert
        assertNotNull(response);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }

    @Test
    void isBlockerOperation_ShouldCallCorrectEndpoint() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");
        BigDecimal amount = new BigDecimal("1500.75");

        when(restTemplateHelper.postForApiResponse(anyString(), any(BigDecimal.class), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = blockerService.isBlockerOperation(amount);

        // Assert
        verify(restTemplateHelper).postForApiResponse(expectedUrl, amount, Void.class);
    }

    @Test
    void isBlockerOperation_WithDifferentGatewayUrls_ShouldConstructCorrectUrl() {
        // Arrange
        String customGatewayUrl = "https://api.example.com";
        BlockerService customBlockerService = new BlockerService(customGatewayUrl, restTemplateHelper);
        BigDecimal amount = new BigDecimal("2000.00");

        try {
            var field = BlockerService.class.getDeclaredField("apiBlocker");
            field.setAccessible(true);
            field.set(customBlockerService, apiBlockerPath);
        } catch (Exception e) {
            fail("Failed to set apiBlocker field via reflection", e);
        }

        String expectedUrl = customGatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(amount), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = customBlockerService.isBlockerOperation(amount);

        // Assert
        verify(restTemplateHelper).postForApiResponse(expectedUrl, amount, Void.class);
    }

    @Test
    void isBlockerOperation_WithDifferentApiBlockerPaths_ShouldConstructCorrectUrl() {
        // Arrange
        String customApiBlockerPath = "/custom-blocker";
        BlockerService customBlockerService = new BlockerService(gatewayUrl, restTemplateHelper);
        BigDecimal amount = new BigDecimal("3000.25");

        try {
            var field = BlockerService.class.getDeclaredField("apiBlocker");
            field.setAccessible(true);
            field.set(customBlockerService, customApiBlockerPath);
        } catch (Exception e) {
            fail("Failed to set apiBlocker field via reflection", e);
        }

        String expectedUrl = gatewayUrl + customApiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(amount), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = customBlockerService.isBlockerOperation(amount);

        // Assert
        verify(restTemplateHelper).postForApiResponse(expectedUrl, amount, Void.class);
    }

    @Test
    void isBlockerOperation_WhenRestTemplateReturnsNull_ShouldHandleNullResponse() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        BigDecimal amount = new BigDecimal("500.50");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(amount), eq(Void.class)))
                .thenReturn(null);

        // Act
        ApiResponse<Void> response = blockerService.isBlockerOperation(amount);

        // Assert
        assertNull(response);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, amount, Void.class);
    }

    @Test
    void isBlockerOperation_WithPreciseDecimalAmount_ShouldPassExactValue() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");
        BigDecimal preciseAmount = new BigDecimal("1234.5678");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(preciseAmount), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = blockerService.isBlockerOperation(preciseAmount);

        // Assert
        assertNotNull(response);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, preciseAmount, Void.class);
    }

    @Test
    void isBlockerOperation_WithVeryLargeAmount_ShouldHandleCorrectly() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");
        BigDecimal veryLargeAmount = new BigDecimal("999999999.99");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), eq(veryLargeAmount), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = blockerService.isBlockerOperation(veryLargeAmount);

        // Assert
        assertNotNull(response);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, veryLargeAmount, Void.class);
    }
}