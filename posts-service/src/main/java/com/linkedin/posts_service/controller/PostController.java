package com.linkedin.posts_service.controller;

import com.linkedin.posts_service.dto.PostCreateDto;
import com.linkedin.posts_service.dto.PostDto;
import com.linkedin.posts_service.entity.Post;
import com.linkedin.posts_service.services.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/core")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;


    @PostMapping
    public ResponseEntity<PostDto> post(@RequestBody PostCreateDto post, HttpServletRequest request) {
        PostDto createdPost =postService.createPost(post,1L);
        return new ResponseEntity<>(createdPost, HttpStatus.CREATED);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostDto> getPost(@PathVariable Long postId) {
        PostDto post=postService.getPostById(postId);
        return new ResponseEntity<>(post, HttpStatus.OK);
    }

    @GetMapping("/users/{userId}/allPosts")
    public ResponseEntity<List<PostDto>> getAllPosts(@PathVariable Long userId) {
        List<PostDto> posts=postService.getAllPostsForUser(userId);
        return ResponseEntity.ok(posts);
    }
}
