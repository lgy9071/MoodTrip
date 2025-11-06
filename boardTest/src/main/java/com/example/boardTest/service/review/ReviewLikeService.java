package com.example.boardTest.service.review;

import com.example.boardTest.entity.review.Review;
import com.example.boardTest.entity.review.ReviewLike;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.review.ReviewLikeRepository;
import com.example.boardTest.repository.review.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    private final ReviewLikeRepository likeRepository;
    private final ReviewRepository reviewRepository;

    @Transactional
    public boolean toggleLike(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        Optional<ReviewLike> existing = likeRepository.findByReviewAndUser(review, user);

        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            return false; // 좋아요 취소
        } else {
            ReviewLike newLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .likedAt(LocalDateTime.now())
                    .build();
            likeRepository.save(newLike);
            return true; // 좋아요 추가
        }
    }

    public long countLikes(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
        return likeRepository.countByReviewId(reviewId);
    }

    public boolean isLiked(Long reviewId, User user) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
        return likeRepository.existsByReviewAndUser(review, user);
    }
}