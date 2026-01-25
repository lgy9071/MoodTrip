package com.example.boardTest.domain.service.review;

import com.example.boardTest.domain.entity.review.Review;
import com.example.boardTest.domain.entity.review.ReviewLike;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.repository.review.ReviewLikeRepository;
import com.example.boardTest.domain.repository.review.ReviewRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewLikeService {

    // 리뷰 좋아요 엔티티 접근용 Repository
    private final ReviewLikeRepository likeRepository;

    // 리뷰 엔티티 조회용 Repository
    private final ReviewRepository reviewRepository;

    /**
     * 리뷰 좋아요 토글
     * - 이미 좋아요가 존재하면 삭제
     * - 존재하지 않으면 새로 생성
     * - 생성/삭제가 한 트랜잭션에서 이루어져야 하므로 @Transactional 적용
     */
    @Transactional
    public boolean toggleLike(Long reviewId, User user) {

        // 대상 리뷰 존재 여부 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰가 존재하지 않습니다.")
                );

        // 해당 리뷰 + 사용자 기준 좋아요 존재 여부 조회
        Optional<ReviewLike> existing =
                likeRepository.findByReviewAndUser(review, user);

        if (existing.isPresent()) {
            // 이미 좋아요가 있으면 삭제
            likeRepository.delete(existing.get());
            return false; // 좋아요 취소
        } else {
            // 좋아요가 없으면 새로 생성
            ReviewLike newLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .likedAt(LocalDateTime.now())
                    .build();

            likeRepository.save(newLike);
            return true; // 좋아요 추가
        }
    }

    /**
     * 특정 리뷰의 좋아요 개수 조회
     */
    public long countLikes(Long reviewId) {

        // 리뷰 존재 여부 확인 (방어 로직)
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰가 존재하지 않습니다.")
                );

        return likeRepository.countByReviewId(reviewId);
    }

    /**
     * 특정 사용자가 리뷰에 좋아요를 눌렀는지 여부
     */
    public boolean isLiked(Long reviewId, User user) {

        // 리뷰 존재 여부 확인
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰가 존재하지 않습니다.")
                );

        return likeRepository.existsByReviewAndUser(review, user);
    }
}