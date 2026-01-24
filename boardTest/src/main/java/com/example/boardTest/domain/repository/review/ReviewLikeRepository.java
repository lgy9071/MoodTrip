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

    /**
     * 특정 사용자가 해당 리뷰에 좋아요를 눌렀는지 여부
     * - 좋아요 버튼 상태 표시용
     */
    boolean existsByReviewAndUser(Review review, User user);

    /**
     * 리뷰 + 사용자 기준 좋아요 엔티티 조회
     * - 좋아요 취소(삭제) 시 사용
     */
    Optional<ReviewLike> findByReviewAndUser(Review review, User user);

    /**
     * 특정 리뷰의 좋아요 수 조회
     * - JPQL로 명시적 COUNT 쿼리 작성
     */
    @Query("SELECT COUNT(rl) FROM ReviewLike rl WHERE rl.review.id = :reviewId")
    long countByReviewId(@Param("reviewId") Long reviewId);
}
