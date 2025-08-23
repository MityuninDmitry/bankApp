package ru.mityunin.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.service.NotificationsService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private static final Logger log = LoggerFactory.getLogger(NotificationController.class);
    private final NotificationsService notificationsService;

    public NotificationController(NotificationsService notificationsService) {
        this.notificationsService = notificationsService;
    }

    @PostMapping("/get")
    public ResponseEntity<List<NotificationDto>> getNotifications() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            log.warn("User not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(List.of());
        }

        try {
            String login = authentication.getName();
            log.info("[FrontUI] NotificationController: {}", login);

            ApiResponse<List<NotificationDto>> apiResponse = notificationsService.getNotifications(login);

            if (apiResponse.isSuccess()) {
                return ResponseEntity.ok(apiResponse.getData());
            } else {
                log.warn("Service returned error: {}", apiResponse.getMessage());
                return ResponseEntity.ok(List.of());
            }

        } catch (Exception e) {
            log.error("Error in getNotifications", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
}
