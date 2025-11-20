package com.example.boardTest.domain.repository.review;

import com.example.boardTest.domain.entity.review.Review;
import com.example.boardTest.domain.entity.review.ReviewLike;
import com.example.boardTest.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByReviewAndUser(Review review, User user);

    Optional<ReviewLike> findByReviewAndUser(Review review, User user);

    @Query("SELECT COUNT(rl) FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    long countByReviewId(@Param("reviewId") Long reviewId);
}