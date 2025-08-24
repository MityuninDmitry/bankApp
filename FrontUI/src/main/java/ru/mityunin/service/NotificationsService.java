package ru.mityunin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.dto.NotificationRequestDto;

import java.util.Arrays;
import java.util.List;

@Service
public class NotificationsService {
    private static final Logger log = LoggerFactory.getLogger(NotificationsService.class);
    private final String serviceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public NotificationsService(
            @Value("${service.url.notifications}") String serviceUrl,
            RestTemplateHelper restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<List<NotificationDto>> getNotifications(String login) {
        NotificationRequestDto requestDto = new NotificationRequestDto();
        requestDto.setLogin(login);
        requestDto.setUsed(false);
        String url = serviceUrl + "/api/notifications";
        ApiResponse<NotificationDto[]> response = restTemplateHelper.postForApiResponse(url, requestDto, NotificationDto[].class);
        return new ApiResponse<>(
                response.isSuccess(),
                response.getMessage(),
                response.getData() != null ? Arrays.asList(response.getData()) : null
        );
    }
}
