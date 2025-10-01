package com.example.boardTest.service;

import com.example.boardTest.entity.Review;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Review saveReview(String title, String content, int rating, User author) {
        Review review = Review.builder()
                .title(title)
                .content(content)
                .rating(rating)
                .author(author)
                .build();
        return reviewRepository.save(review);
    }

    public Page<Review> getReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return reviewRepository.findAll(pageable);
    }

    public Review getReview(Long id) {
        return reviewRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("해당 리뷰가 존재하지 않습니다."));
    }

    public void deleteReview(Long id, User currentUser) {
        Review review = getReview(id);
        if (!review.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }
}