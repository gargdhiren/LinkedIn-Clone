package com.linkedin.posts_service.repository;

import com.linkedin.posts_service.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikesRepository extends JpaRepository<PostLike,Long> {
    boolean existsByPostIdAndUserId(Long postId,Long userId);

    void deleteByUserIdAndPostId(Long userId, Long postId);
}
