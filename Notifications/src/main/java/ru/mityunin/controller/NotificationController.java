package ru.mityunin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AddNotificationRequestDto;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.dto.NotificationRequestDto;
import ru.mityunin.service.NotificationService;

import java.util.List;

@Controller
@RequestMapping("/api")
public class NotificationController {
    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping("/notifications")
    public ResponseEntity<ApiResponse<List<NotificationDto>>> notifications(@RequestBody NotificationRequestDto request) {
        ApiResponse<List<NotificationDto>> apiResponse = service.notificationsBy(request);
        return ResponseEntity.status(HttpStatus.OK).body(apiResponse);
    }

    @PostMapping("/addNotification")
    public ResponseEntity<ApiResponse<Void>> addNotification(@RequestBody AddNotificationRequestDto requestDto) {
        ApiResponse<Void> response =  service.addNotification(requestDto);
        if (!response.isSuccess()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }

    }
}
