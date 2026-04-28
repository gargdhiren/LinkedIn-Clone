package com.linkedin.posts_service.clients;

import com.linkedin.posts_service.dto.PersonsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name ="CONNECTIONS-SERVICE",path="connections")
public interface ConnectionsClient {
    @GetMapping("/core/first-degree")
    List<PersonsDto> getFirstConnections();
}
