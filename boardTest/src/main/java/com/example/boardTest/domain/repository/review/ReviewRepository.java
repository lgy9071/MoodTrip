package com.example.boardTest.domain.repository.review;

import com.example.boardTest.domain.entity.review.Review;
import com.example.boardTest.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 전체 리뷰 조회 (페이징)
     */
    Page<Review> findAll(Pageable pageable);

    /**
     * 카테고리별 리뷰 조회
     * - movie / trip / place
     */
    Page<Review> findByCategory(String category, Pageable pageable);

    /**
     * 특정 작성자의 리뷰 조회
     * - 마이페이지, 내 리뷰 목록
     */
    Page<Review> findByAuthor(User author, Pageable pageable);

    /**
     * 작성자 + 카테고리 조건 조회
     * - 내 리뷰 + 특정 카테고리 필터
     */
    Page<Review> findByAuthorAndCategory(
            User author,
            String category,
            Pageable pageable
    );
}
