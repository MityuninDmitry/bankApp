package ru.mityunin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.AuthenticatedRestTemplateService;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AddNotificationRequestDto;

@Service
public class NotificationService {

    private final String serviceUrl;
    private final String apiNotifications;
    private final AuthenticatedRestTemplateService restTemplateHelper;

    public NotificationService(
            @Value("${service.url.gateway}") String serviceUrl,
            @Value("${service.api.notifications}") String apiNotifications,
            AuthenticatedRestTemplateService restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.apiNotifications = apiNotifications;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> sendNotification(String login, String message) {
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto();
        requestDto.setLogin(login);
        requestDto.setMessage(message);
        String requestUrl = serviceUrl + apiNotifications + "/api/addNotification";
        return restTemplateHelper.postForApiResponse(requestUrl, requestDto, Void.class);
    }
}
