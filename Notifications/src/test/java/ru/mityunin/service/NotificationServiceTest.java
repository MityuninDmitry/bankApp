package ru.mityunin.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ru.mityunin.NotificationRepository;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AddNotificationRequestDto;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.dto.NotificationRequestDto;
import ru.mityunin.mapper.NotificationMapper;
import ru.mityunin.model.Notification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository repository;

    @Mock
    private NotificationMapper mapper;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void notificationsBy_ShouldReturnNotifications_WhenValidRequest() {
        // Arrange
        NotificationRequestDto request = new NotificationRequestDto("testUser", false);
        List<Notification> notifications = List.of(
                new Notification(1L, LocalDateTime.now(), "Message 1", "testUser", false),
                new Notification(2L, LocalDateTime.now(), "Message 2", "testUser", false)
        );

        List<NotificationDto> notificationDtos = List.of(
                new NotificationDto(1L, LocalDateTime.now(), "Message 1"),
                new NotificationDto(2L, LocalDateTime.now(), "Message 2")
        );

        when(repository.findByLoginAndUsedOrderByLocalDateTimeAsc("testUser", false))
                .thenReturn(notifications);
        when(mapper.notificationToNotificationDto(any(Notification.class)))
                .thenReturn(notificationDtos.get(0), notificationDtos.get(1));
        when(repository.saveAll(anyList())).thenReturn(notifications);

        // Act
        ApiResponse<List<NotificationDto>> response = notificationService.notificationsBy(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().size());
        verify(repository, times(1)).findByLoginAndUsedOrderByLocalDateTimeAsc("testUser", false);
        verify(repository, times(1)).saveAll(anyList());
    }

    @Test
    void notificationsBy_ShouldReturnEmptyList_WhenNoNotificationsFound() {
        // Arrange
        NotificationRequestDto request = new NotificationRequestDto("unknownUser", false);

        // Act
        ApiResponse<List<NotificationDto>> response = notificationService.notificationsBy(request);

        // Assert
        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
        verify(repository, times(1)).findByLoginAndUsedOrderByLocalDateTimeAsc("unknownUser", false);
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void addNotification_ShouldSuccess_WhenValidRequest() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto("testUser", "Test message");
        Notification notification = new Notification();
        notification.setLogin("testUser");
        notification.setMessage("Test message");
        notification.setUsed(false);
        notification.setLocalDateTime(LocalDateTime.now());

        when(repository.save(any(Notification.class))).thenReturn(notification);

        // Act
        ApiResponse<Void> response = notificationService.addNotification(requestDto);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Успех добавления нотификации ", response.getMessage());
        verify(repository, times(1)).save(any(Notification.class));
    }

    @Test
    void addNotification_ShouldReturnError_WhenMessageIsEmpty() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto("testUser", "");

        // Act
        ApiResponse<Void> response = notificationService.addNotification(requestDto);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Message cannot be empty"));
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void addNotification_ShouldReturnError_WhenMessageIsNull() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto("testUser", null);

        // Act
        ApiResponse<Void> response = notificationService.addNotification(requestDto);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Message cannot be empty"));
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void addNotification_ShouldReturnError_WhenLoginIsEmpty() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto("", "Test message");

        // Act
        ApiResponse<Void> response = notificationService.addNotification(requestDto);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Login cannot be empty"));
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void addNotification_ShouldReturnError_WhenLoginIsNull() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto(null, "Test message");

        // Act
        ApiResponse<Void> response = notificationService.addNotification(requestDto);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Login cannot be empty"));
        verify(repository, never()).save(any(Notification.class));
    }

    @Test
    void addNotification_ShouldHandleRepositoryException() {
        // Arrange
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto("testUser", "Test message");
        when(repository.save(any(Notification.class))).thenThrow(new RuntimeException("Database error"));

        // Act
        ApiResponse<Void> response = notificationService.addNotification(requestDto);

        // Assert
        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("Database error"));
    }
}