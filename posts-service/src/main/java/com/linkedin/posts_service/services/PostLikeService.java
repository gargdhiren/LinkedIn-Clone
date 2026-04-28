package com.linkedin.posts_service.services;

import com.linkedin.common.events.PostLikedEvent;
import com.linkedin.posts_service.auth.UserContextHolder;
import com.linkedin.posts_service.entity.Post;
import com.linkedin.posts_service.entity.PostLike;
import com.linkedin.posts_service.exception.BadRequestException;
import com.linkedin.posts_service.repository.PostLikesRepository;
import com.linkedin.posts_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostLikeService {
    private final PostLikesRepository postLikesRepository;
    private final PostRepository postRepository;
    private final KafkaTemplate<Long, PostLikedEvent> kafkaTemplate;

    @Transactional
    public void likePost(Long postId) {
        log.info("Attempting to like the post with id: {}",postId);

        Post post=postRepository.findById(postId).orElseThrow(()->new ResourceAccessException("Post not found with id: "+postId));


        Long userId= UserContextHolder.getCurrentUserId();

        boolean liked = postLikesRepository.existsByPostIdAndUserId(postId,userId);
        if(liked) {
           throw new BadRequestException("Post with id: "+postId+" is already liked by user with id: "+userId);
        }
        PostLike postLike = new PostLike();
        postLike.setPostId(postId);
        postLike.setUserId(userId);

        postLikesRepository.save(postLike);

        log.info("Successfully liked the post with id: {}",postId);

        PostLikedEvent postLikedEvent=PostLikedEvent.builder().postId(postId).likedByUserId(userId).creatorId(post.getUserId()).build();
        kafkaTemplate.send("post-liked-topic",postId, postLikedEvent);
    }

    @Transactional
    public void unlikePost(Long postId) {
        log.info("Attempting to unlike the post with id: {}",postId);

        boolean exists = postRepository.existsById(postId);
        if (!exists) {
            throw new ResourceAccessException("Post not found with id: "+postId);
        }

        Long userId= UserContextHolder.getCurrentUserId();

        boolean liked = postLikesRepository.existsByPostIdAndUserId(postId,userId);
        if(!liked) {
            throw new BadRequestException("Post with id: "+postId+" is already unliked by user with id: "+userId);
        }
        postLikesRepository.deleteByUserIdAndPostId(userId,postId);

        log.info("Successfully unliked the post with id: {}",postId);
    }
}
