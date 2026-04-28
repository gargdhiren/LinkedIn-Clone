package com.linkedin.notification_service.consumer;

import com.linkedin.common.events.AcceptConnectionRequestEvent;
import com.linkedin.common.events.SendConnectionRequestEvent;
import com.linkedin.notification_service.clients.ConnectionsClient;
import com.linkedin.notification_service.service.SendNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConnectionsServiceConsumer {
    private final SendNotification sendNotificationService;

    @KafkaListener(topics = "accept-connection-request-topic")
    public void handleAcceptConnectionRequestTopic(AcceptConnectionRequestEvent acceptConnectionRequestEvent) {
        log.info("Received accept-connection-request-topic");
        String message = "Your request is accepted by user id : %d" + acceptConnectionRequestEvent.getReceiverId();
        sendNotificationService.sendNotification(acceptConnectionRequestEvent.getSenderId(), message);
    }

    @KafkaListener(topics = "send-connection-request-topic")
    public void handleSendConnectionRequestTopic(SendConnectionRequestEvent sendConnectionRequestEvent) {
        log.info("Received send-connection-request-topic");
        String message = "You have received an connection request from  user id : %d" + sendConnectionRequestEvent.getSenderId();
        sendNotificationService.sendNotification(sendConnectionRequestEvent.getReceiverId(), message);
    }


}
