package com.linkedin.connections_service.service;

import com.linkedin.common.events.AcceptConnectionRequestEvent;
import com.linkedin.common.events.SendConnectionRequestEvent;
import com.linkedin.connections_service.auth.UserContextHolder;
import com.linkedin.connections_service.entity.Persons;
import com.linkedin.connections_service.repository.PersonsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ConnectionsService {
    private final PersonsRepository personsRepository;
    private final KafkaTemplate<Long, AcceptConnectionRequestEvent> acceptConnectionRequestEventKafkaTemplate;
    private final KafkaTemplate<Long, SendConnectionRequestEvent> sendConnectionRequestEventKafkaTemplate;

    public void addPerson(Long userId, String name){
        personsRepository.createPerson(userId, name);
    }

    public List<Persons> getFirstDegreeConnections(){
        Long userId= UserContextHolder.getCurrentUserId();
        log.info("Getting first degree connections for user {}",userId);

        return personsRepository.getFirstDegreeConnections(userId);
    }

    public Boolean sendConnectionRequest(Long receiverId) {
        Long senderId=UserContextHolder.getCurrentUserId();
        boolean alreadySentRequest = personsRepository.connectionRequestExists(senderId, receiverId);
        log.info("Trying to send connection request, sender: {}, receiver: {}",senderId,receiverId);

        if(senderId.equals(receiverId)){
            throw new RuntimeException("Sender and Receiver are the same");
        }


        if(alreadySentRequest){
            throw new RuntimeException("Connection request already exists");
        }

        boolean alreadyConnected = personsRepository.alreadyConnected(senderId, receiverId);

        if(alreadyConnected){
            throw new RuntimeException("Already connected");
        }

        log.info("Successfully sent connection request for sender: {}, receiver: {}",senderId,receiverId);

        personsRepository.addConnectionRequest(senderId, receiverId);

        SendConnectionRequestEvent sendConnectionRequestEvent = SendConnectionRequestEvent.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();

        sendConnectionRequestEventKafkaTemplate.send("send-connection-request-topic",sendConnectionRequestEvent);
        return true;
    }

    public  Boolean acceptConnectionRequest(Long senderId) {
        Long receiverId=UserContextHolder.getCurrentUserId();
        log.info("Connection request for sender: {}, receiver: {}",senderId,receiverId);

        boolean connectionRequestExists=personsRepository.connectionRequestExists(senderId, receiverId);
        if(!connectionRequestExists){
            throw new RuntimeException("Connection request does not exist");
        }

        log.info("Connection request for sender: {}, receiver: {} is accepted.",senderId,receiverId);
        personsRepository.acceptConnectionRequest(senderId, receiverId);

        AcceptConnectionRequestEvent acceptConnectionRequestEvent = AcceptConnectionRequestEvent.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .build();

        acceptConnectionRequestEventKafkaTemplate.send("accept-connection-request-topic",acceptConnectionRequestEvent);
        return true;
    }

    public Boolean rejectConnectRequest(Long senderId) {
        Long receiverId=UserContextHolder.getCurrentUserId();
        log.info("Connection request for sender: {}, receiver: {}",senderId,receiverId);

        boolean connectionRequestExists=personsRepository.connectionRequestExists(senderId, receiverId);
        if(!connectionRequestExists){
            throw new RuntimeException("Connection request does not exist");
        }

        log.info("Connection request for sender: {}, receiver: {} is rejected.",senderId,receiverId);

        personsRepository.rejectConnectionRequest(senderId, receiverId);
        return true;
    }
}
