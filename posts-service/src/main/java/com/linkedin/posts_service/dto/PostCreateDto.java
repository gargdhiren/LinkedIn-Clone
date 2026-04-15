package com.linkedin.posts_service.dto;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostCreateDto {
    private String content;
}
