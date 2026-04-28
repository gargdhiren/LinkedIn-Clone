package com.linkedin.notification_service.consumer;

import com.linkedin.common.events.PostCreatedEvent;
import com.linkedin.common.events.PostLikedEvent;
import com.linkedin.notification_service.clients.ConnectionsClient;
import com.linkedin.notification_service.dto.PersonsDto;
import com.linkedin.notification_service.entity.Notification;
import com.linkedin.notification_service.repository.NotificationRepository;
import com.linkedin.notification_service.service.SendNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostsServiceConsumer {

    private final ConnectionsClient connectionsClient;
    private final SendNotification sendNotificationService;

    @KafkaListener(topics = "post-created-topic")
    public void handleTopic(PostCreatedEvent postCreatedEvent) {
        log.info("Sending notification: handlePostCreated: {}", postCreatedEvent);
        List<PersonsDto> connections=connectionsClient.getFirstConnections(postCreatedEvent.getCreatorId());

        for(PersonsDto connection:connections){
            sendNotificationService.sendNotification(connection.getUserId(),"Go check the post created by "+postCreatedEvent.getCreatorId());
        }

    }

    @KafkaListener(topics="post-liked-topic")
    public void handlePostLiked(PostLikedEvent postLikedEvent) {
        log.info("Sending notification: handlePostLiked: {}", postLikedEvent);
        String message=String.format("Your post, %d has been liked by %d",postLikedEvent.getPostId(),postLikedEvent.getLikedByUserId());
        sendNotificationService.sendNotification(postLikedEvent.getCreatorId(),message);
    }
}
