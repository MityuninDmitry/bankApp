package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AddNotificationRequestDto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private AuthenticatedRestTemplateService restTemplateHelper;

    private NotificationService notificationService;

    private final String gatewayUrl = "http://localhost:8080";
    private final String apiNotificationsPath = "/notifications";
    private final String testLogin = "testUser";
    private final String testMessage = "Test notification message";

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(gatewayUrl, restTemplateHelper);

        // Устанавливаем значение apiNotifications через рефлексию
        try {
            var field = NotificationService.class.getDeclaredField("apiNotifications");
            field.setAccessible(true);
            field.set(notificationService, apiNotificationsPath);
        } catch (Exception e) {
            fail("Failed to set apiNotifications field via reflection", e);
        }
    }

    @Test
    void sendNotification_WhenSuccess_ShouldReturnSuccessResponse() {
        // Arrange
        String expectedUrl = gatewayUrl + apiNotificationsPath + "/api/addNotification";
        ApiResponse<Void> expectedResponse = ApiResponse.success("Notification sent successfully");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), any(AddNotificationRequestDto.class), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> actualResponse = notificationService.sendNotification(testLogin, testMessage);

        // Assert
        assertNotNull(actualResponse);
        assertTrue(actualResponse.isSuccess());
        assertEquals("Notification sent successfully", actualResponse.getMessage());

        verify(restTemplateHelper).postForApiResponse(
                eq(expectedUrl),
                argThat((AddNotificationRequestDto request) ->
                        request.getLogin().equals(testLogin) &&
                                request.getMessage().equals(testMessage)
                ),
                eq(Void.class)
        );
    }

    @Test
    void sendNotification_WhenServiceReturnsError_ShouldReturnErrorResponse() {
        // Arrange
        String expectedUrl = gatewayUrl + apiNotificationsPath + "/api/addNotification";
        ApiResponse<Void> expectedResponse = ApiResponse.error("Failed to send notification");

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), any(AddNotificationRequestDto.class), eq(Void.class)))
                .thenReturn(expectedResponse);

        // Act
        ApiResponse<Void> actualResponse = notificationService.sendNotification(testLogin, testMessage);

        // Assert
        assertNotNull(actualResponse);
        assertFalse(actualResponse.isSuccess());
        assertEquals("Failed to send notification", actualResponse.getMessage());

        verify(restTemplateHelper).postForApiResponse(eq(expectedUrl), any(AddNotificationRequestDto.class), eq(Void.class));
    }

    @Test
    void sendNotification_WhenServiceThrowsException_ShouldPropagateException() {
        // Arrange
        String expectedUrl = gatewayUrl + apiNotificationsPath + "/api/addNotification";

        when(restTemplateHelper.postForApiResponse(eq(expectedUrl), any(AddNotificationRequestDto.class), eq(Void.class)))
                .thenThrow(new RuntimeException("Notification service unavailable"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            notificationService.sendNotification(testLogin, testMessage);
        });

        assertEquals("Notification service unavailable", exception.getMessage());
        verify(restTemplateHelper).postForApiResponse(eq(expectedUrl), any(AddNotificationRequestDto.class), eq(Void.class));
    }

}