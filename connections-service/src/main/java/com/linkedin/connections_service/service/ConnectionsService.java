package com.linkedin.connections_service.service;


import com.linkedin.connections_service.entity.Persons;
import com.linkedin.connections_service.repository.PersonsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class ConnectionsService {
    private final PersonsRepository personsRepository;

    public List<Persons> getFirstDegreeConnections(Long userId){
        log.info("Getting first degree connections for user {}",userId);

        return personsRepository.getFirstDegreeConnections(userId);
    }
}
