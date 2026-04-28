package com.linkedin.connections_service.controllers;

import com.linkedin.connections_service.auth.UserContextHolder;
import com.linkedin.connections_service.entity.Persons;
import com.linkedin.connections_service.service.ConnectionsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
public class ConnectionsController {

    private final ConnectionsService connectionsService;

    @GetMapping("/first-degree")
    public ResponseEntity<List<Persons>> getFirstConnections() {
        return ResponseEntity.ok(connectionsService.getFirstDegreeConnections());
    }

    @PostMapping("/request/{userId}")
    public ResponseEntity<Boolean> sendConnectionRequest(@PathVariable Long userId) {
        return ResponseEntity.ok(connectionsService.sendConnectionRequest(userId));
    }

    @PostMapping("/accept/{userId}")
    public ResponseEntity<Boolean> acceptConnectionRequest(@PathVariable Long userId) {
        return ResponseEntity.ok(connectionsService.acceptConnectionRequest(userId));
    }
    
    @PostMapping("/reject/{userId}")
    public  ResponseEntity<Boolean> rejectConnectionRequest(@PathVariable Long userId) {
        return ResponseEntity.ok(connectionsService.rejectConnectRequest(userId));
    }
}
