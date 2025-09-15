package ru.mityunin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AddNotificationRequestDto;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.dto.NotificationRequestDto;
import ru.mityunin.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationController notificationController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void notifications_ShouldReturnOkStatus_WhenServiceReturnsSuccess() {
        // Arrange
        NotificationRequestDto request = new NotificationRequestDto("testUser", false);
        List<NotificationDto> notificationList = List.of(
                new NotificationDto(1L, LocalDateTime.now(), "Test message 1"),
                new NotificationDto(2L, LocalDateTime.now(), "Test message 2")
        );
        ApiResponse<List<NotificationDto>> apiResponse = ApiResponse.success("Success", notificationList);

        when(notificationService.notificationsBy(any(NotificationRequestDto.class)))
                .thenReturn(apiResponse);

        // Act
        ResponseEntity<ApiResponse<List<NotificationDto>>> response =
                notificationController.notifications(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals(2, response.getBody().getData().size());
        verify(notificationService, times(1)).notificationsBy(request);
    }

    @Test
    void addNotification_ShouldReturnOkStatus_WhenServiceReturnsSuccess() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto("testUser", "Test message");
        ApiResponse<Void> apiResponse = ApiResponse.success("Успех добавления нотификации");

        when(notificationService.addNotification(any(AddNotificationRequestDto.class)))
                .thenReturn(apiResponse);

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                notificationController.addNotification(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        verify(notificationService, times(1)).addNotification(requestDto);
    }

    @Test
    void addNotification_ShouldReturnInternalServerError_WhenServiceReturnsError() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto("testUser", "Test message");
        ApiResponse<Void> apiResponse = ApiResponse.error("Ошибка добавления");

        when(notificationService.addNotification(any(AddNotificationRequestDto.class)))
                .thenReturn(apiResponse);

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                notificationController.addNotification(requestDto);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        verify(notificationService, times(1)).addNotification(requestDto);
    }

    @Test
    void addNotification_ShouldHandleNullRequest() {
        // Arrange
        ApiResponse<Void> apiResponse = ApiResponse.error("Ошибка: Message cannot be empty");

        when(notificationService.addNotification(null))
                .thenReturn(apiResponse);

        // Act
        ResponseEntity<ApiResponse<Void>> response =
                notificationController.addNotification(null);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }
}