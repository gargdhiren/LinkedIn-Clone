package com.linkedin.notification_service.clients;

import com.linkedin.notification_service.dto.PersonsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name ="CONNECTIONS-SERVICE",path="connections")
public interface ConnectionsClient {
    @GetMapping("/core/first-degree")
    List<PersonsDto> getFirstConnections(@RequestHeader("X-User-Id") Long userId);
}
