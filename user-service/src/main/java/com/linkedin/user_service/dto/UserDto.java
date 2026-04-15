package com.linkedin.user_service.dto;

import jakarta.persistence.Column;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
}
