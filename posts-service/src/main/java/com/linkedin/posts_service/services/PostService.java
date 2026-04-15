package com.linkedin.posts_service.services;

import com.linkedin.posts_service.dto.PostCreateDto;
import com.linkedin.posts_service.dto.PostDto;
import com.linkedin.posts_service.entity.Post;
import com.linkedin.posts_service.exception.ResourceNotFoundException;
import com.linkedin.posts_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;

    public PostDto createPost(PostCreateDto postCreateDto,Long userId) {
        Post post = modelMapper.map(postCreateDto, Post.class);
        post.setUserId(userId);

        Post savedPost=postRepository.save(post);
        return modelMapper.map(savedPost, PostDto.class);
    }

    public PostDto getPostById(Long postId) {
        log.debug("Getting post by id: {}", postId);
        Post post=postRepository.findById(postId).orElseThrow(()->new ResourceNotFoundException("No post with id: "+postId));
        return modelMapper.map(post, PostDto.class);
    }

    public List<PostDto> getAllPostsForUser(Long userId) {
        List<Post> posts=postRepository.findAllByUserId(userId);
        return posts.stream().map(post->modelMapper.map(post,PostDto.class)).collect(Collectors.toList());
    }
}
