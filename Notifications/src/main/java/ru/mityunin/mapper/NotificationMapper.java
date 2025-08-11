package ru.mityunin.mapper;

import org.mapstruct.Mapper;
import ru.mityunin.dto.NotificationDto;
import ru.mityunin.model.Notification;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationDto notificationToNotificationDto(Notification notification);
    Notification notificationDtoToNotification(NotificationDto notificationDto);
}
