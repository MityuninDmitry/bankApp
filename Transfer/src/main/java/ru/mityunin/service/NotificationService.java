package ru.mityunin.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.common.dto.RestTemplateHelper;
import ru.mityunin.dto.AddNotificationRequestDto;

@Service
public class NotificationService {

    private final String serviceUrl;
    private final RestTemplateHelper restTemplateHelper;

    public NotificationService(@Value("${service.notifications.url}") String serviceUrl, RestTemplateHelper restTemplateHelper) {
        this.serviceUrl = serviceUrl;
        this.restTemplateHelper = restTemplateHelper;
    }

    public ApiResponse<Void> sendNotification(String login, String message) {
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto();
        requestDto.setLogin(login);
        requestDto.setMessage(message);
        String requestUrl = serviceUrl + "/api/addNotification";
        return restTemplateHelper.postForApiResponse(requestUrl, requestDto, Void.class);
    }
}
