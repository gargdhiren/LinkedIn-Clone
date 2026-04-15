package com.linkedin.user_service.controller;


import com.linkedin.user_service.dto.LoginDto;
import com.linkedin.user_service.dto.SignUpDto;
import com.linkedin.user_service.dto.UserDto;
import com.linkedin.user_service.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.parser.Entity;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody SignUpDto user){
        UserDto userDto = authService.signUp(user);

        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginDto loginDto){
        String token = authService.login(loginDto);
        return ResponseEntity.ok(token);
    }
}
