package com.linkedin.notification_service.service;


import com.linkedin.notification_service.entity.Notification;
import com.linkedin.notification_service.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SendNotification {
    private final NotificationRepository notificationRepository;

    public void sendNotification(Long userId,String message) {
        log.info("Sending notification");
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setUserId(userId);

        notificationRepository.save(notification);
    }
}
