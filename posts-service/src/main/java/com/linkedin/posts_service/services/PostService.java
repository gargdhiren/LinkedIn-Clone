package com.linkedin.posts_service.services;

import com.linkedin.common.events.PostCreatedEvent;
import com.linkedin.posts_service.auth.UserContextHolder;
import com.linkedin.posts_service.clients.ConnectionsClient;
import com.linkedin.posts_service.dto.PersonsDto;
import com.linkedin.posts_service.dto.PostCreateDto;
import com.linkedin.posts_service.dto.PostDto;
import com.linkedin.posts_service.entity.Post;
import com.linkedin.posts_service.exception.ResourceNotFoundException;
import com.linkedin.posts_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final ModelMapper modelMapper;
    private final ConnectionsClient connectionsClient;
    private final KafkaTemplate<Long, PostCreatedEvent> kafkaTemplate;

    public PostDto createPost(PostCreateDto postCreateDto) {
        Long userId = UserContextHolder.getCurrentUserId();
        Post post = modelMapper.map(postCreateDto, Post.class);
        post.setUserId(userId);

        Post savedPost=postRepository.save(post);

        PostCreatedEvent postCreatedEvent= PostCreatedEvent.builder()
                .postId(savedPost.getId())
                .creatorId(userId)
                .content(savedPost.getContent())
                .build();

        kafkaTemplate.send("post-created-topic",postCreatedEvent);

        return modelMapper.map(savedPost, PostDto.class);
    }

    public PostDto getPostById(Long postId) {
        log.debug("Getting post by id: {}", postId);

        List<PersonsDto> firstConnections= connectionsClient.getFirstConnections();

        Post post=postRepository.findById(postId).orElseThrow(()->new ResourceNotFoundException("No post with id: "+postId));
        return modelMapper.map(post, PostDto.class);
    }

    public List<PostDto> getAllPostsForUser(Long userId) {
        List<Post> posts=postRepository.findAllByUserId(userId);
        return posts.stream().map(post->modelMapper.map(post,PostDto.class)).collect(Collectors.toList());
    }
}
