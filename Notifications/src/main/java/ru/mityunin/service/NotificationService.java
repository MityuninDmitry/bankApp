package ru.mityunin.service;

import org.springframework.stereotype.Service;
import ru.mityunin.NotificationRepository;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.dto.NotificationRequestDto;
import ru.mityunin.mapper.NotificationMapper;
import ru.mityunin.model.Notification;

import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    public NotificationService(NotificationRepository repository, NotificationMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public ApiResponse<List<NotificationDto>> notificationsBy(NotificationRequestDto request) {
        List<Notification> list = repository.findByLoginAndUsedOrderByLocalDateTimeAsc(request.getLogin(), request.getUsed());
        List<NotificationDto> notificationDtoList = list.stream()
                .map(mapper::notificationToNotificationDto)
                .toList();
        return ApiResponse.success("Список нотификаций",notificationDtoList);
    }
}
