package com.linkedin.connections_service.consumer;

import com.linkedin.common.events.UserCreatedEvent;
import com.linkedin.connections_service.service.ConnectionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceConsumer {
    private final ConnectionsService connectionsService;

    @KafkaListener(topics= "user-created-event")
    public void handleUserCreatedEvent(UserCreatedEvent userCreatedEvent){
        log.info("Received user-created event topic");
        connectionsService.addPerson(userCreatedEvent.getUserId(), userCreatedEvent.getName());
    }
}
