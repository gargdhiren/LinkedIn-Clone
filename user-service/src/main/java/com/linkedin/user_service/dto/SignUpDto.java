package com.linkedin.user_service.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class SignUpDto {
    private String name;
    private String email;
    private String password;
}
