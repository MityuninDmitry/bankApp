package ru.mityunin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mityunin.model.Notification;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByLoginAndUsedOrderByLocalDateTimeAsc(String login, Boolean used);
}
