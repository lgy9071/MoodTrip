package com.example.boardTest.domain.repository.review;

import com.example.boardTest.domain.entity.review.Review;
import com.example.boardTest.domain.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    Page<Review> findAll(Pageable pageable);

    Page<Review> findByCategory(String category, Pageable pageable);

    Page<Review> findByAuthor(User author, Pageable pageable);

    Page<Review> findByAuthorAndCategory(User author, String category, Pageable pageable);
}