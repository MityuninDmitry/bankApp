package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;

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
        // Используем рефлексию для установки значения apiBlocker, так как оно инжектится через @Value
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

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), isNull(), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> actualResponse = blockerService.isBlockerOperation();

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Operation allowed", actualResponse.getMessage());

        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }

    @Test
    void isBlockerOperation_WhenServiceReturnsError_ShouldReturnErrorResponse() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.error("Operation blocked");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), isNull(), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> actualResponse = blockerService.isBlockerOperation();

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isSuccess());
        assertEquals("Operation blocked", actualResponse.getMessage());

        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }

    @Test
    void isBlockerOperation_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), isNull(), eq(Void.class)))
                .thenThrow(new RuntimeException("Service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            blockerService.isBlockerOperation();
        });

        assertEquals("Service unavailable", exception.getMessage());
        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }

    @Test
    void isBlockerOperation_ShouldCallCorrectEndpoint() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");

        when(restTemplateHelper.postForApiResponse(anyString(), isNull(), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = blockerService.isBlockerOperation();

        // Assert
        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }

    @Test
    void isBlockerOperation_WithDifferentGatewayUrls_ShouldConstructCorrectUrl() {
        // Arrange
        String customGatewayUrl = "https://api.example.com";
        BlockerService customBlockerService = new BlockerService(customGatewayUrl, restTemplateHelper);

        try {
            var field = BlockerService.class.getDeclaredField("apiBlocker");
            field.setAccessible(true);
            field.set(customBlockerService, apiBlockerPath);
        } catch (Exception e) {
            fail("Failed to set apiBlocker field via reflection", e);
        }

        String expectedUrl = customGatewayUrl + apiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), isNull(), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = customBlockerService.isBlockerOperation();

        // Assert
        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }

    @Test
    void isBlockerOperation_WithDifferentApiBlockerPaths_ShouldConstructCorrectUrl() {
        // Arrange
        String customApiBlockerPath = "/custom-blocker";
        BlockerService customBlockerService = new BlockerService(gatewayUrl, restTemplateHelper);

        try {
            var field = BlockerService.class.getDeclaredField("apiBlocker");
            field.setAccessible(true);
            field.set(customBlockerService, customApiBlockerPath);
        } catch (Exception e) {
            fail("Failed to set apiBlocker field via reflection", e);
        }

        String expectedUrl = gatewayUrl + customApiBlockerPath + "/api/isBlockerOperation";
        ApiResponse<Void> expectedResponse = ApiResponse.success("OK");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), isNull(), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> response = customBlockerService.isBlockerOperation();

        // Assert
        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }

    @Test
    void isBlockerOperation_WhenRestTemplateReturnsNull_ShouldHandleNullResponse() {
        // Arrange
        String expectedUrl = gatewayUrl + apiBlockerPath + "/api/isBlockerOperation";

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), isNull(), eq(Void.class)))
                .thenReturn(null);

        // Act
        ApiResponse<Void> response = blockerService.isBlockerOperation();

        // Assert
        assertNull(response);
        verify(restTemplateHelper).postForApiResponse(expectedUrl, null, Void.class);
    }
}