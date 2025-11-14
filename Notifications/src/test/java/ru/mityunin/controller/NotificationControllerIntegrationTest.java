package ru.mityunin.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AddNotificationRequestDto;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.dto.NotificationRequestDto;
import ru.mityunin.service.NotificationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class NotificationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Test
    void notificationsEndpoint_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        NotificationRequestDto request = new NotificationRequestDto("testUser", false);
        List<NotificationDto> notifications = List.of(
                new NotificationDto(1L, LocalDateTime.now(), "Test message")
        );
        ApiResponse<List<NotificationDto>> apiResponse = ApiResponse.success("Success", notifications);

        when(notificationService.notificationsBy(any(NotificationRequestDto.class)))
                .thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/notifications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"testUser\",\"used\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].message").value("Test message"));
    }

    @Test
    void addNotificationEndpoint_ShouldReturnOk_WhenValidRequest() throws Exception {
        // Arrange
        ApiResponse<Void> apiResponse = ApiResponse.success("Успех добавления нотификации");

        when(notificationService.addNotification(any(AddNotificationRequestDto.class)))
                .thenReturn(apiResponse);

        // Act & Assert
        mockMvc.perform(post("/api/addNotification")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"login\":\"testUser\",\"message\":\"Test message\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}