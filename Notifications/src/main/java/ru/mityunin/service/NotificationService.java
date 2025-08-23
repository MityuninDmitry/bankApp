package ru.mityunin.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mityunin.NotificationRepository;
import ru.mityunin.common.dto.ApiResponse;
import ru.mityunin.dto.AddNotificationRequestDto;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.dto.NotificationRequestDto;
import ru.mityunin.mapper.NotificationMapper;
import ru.mityunin.model.Notification;

import java.time.LocalDateTime;
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

        repository.saveAll(
                list.stream().map(notification -> {
                    notification.setUsed(true);
                    return notification;
                }).toList()
        );

        return ApiResponse.success("Список нотификаций",notificationDtoList);
    }

    public ApiResponse<Void> addNotification(AddNotificationRequestDto requestDto) {
        try {
            Notification notification = new Notification();
            notification.setLogin(requestDto.getLogin());
            notification.setUsed(false);
            notification.setMessage(requestDto.getMessage());
            notification.setLocalDateTime(LocalDateTime.now());
            repository.save(notification);
            return ApiResponse.success("Успех добавления нотификации");
        } catch (Exception e) {
            return ApiResponse.error("Ошибка: " + e.getMessage());
        }

    }

    //testUser
    //@Scheduled(fixedRate = 5000)  // fixedRate = 1 секунда (1000 мс)
    @Transactional
    public void generate() {
        AddNotificationRequestDto requestDto = new AddNotificationRequestDto();
        requestDto.setMessage("Hello, world");
        requestDto.setLogin("testUser");
        addNotification(requestDto);

        requestDto = new AddNotificationRequestDto();
        requestDto.setMessage("Hello, world 2");
        requestDto.setLogin("testUser");
        addNotification(requestDto);
    }
}
