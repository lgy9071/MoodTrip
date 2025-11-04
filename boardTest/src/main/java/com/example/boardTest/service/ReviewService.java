package com.example.boardTest.service;

import com.example.boardTest.dto.review.ReviewListDTO;
import com.example.boardTest.entity.Review;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.ReviewLikeRepository;
import com.example.boardTest.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;

    private final Path uploadDir = Paths.get("uploads/reviews"); // 저장 폴더

    public Review saveReview(String title, String content, int rating,
                             User author, String category, Long targetId, MultipartFile image) {

        String imagePath = saveImage(image);

        Review review = Review.builder()
                .title(title)
                .content(content)
                .rating(rating)
                .author(author)
                .category(category)
                .targetId(targetId)
                .imageUrl(imagePath)
                .build();

        return reviewRepository.save(review);
    }

    public Page<ReviewListDTO> getReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Review> reviewPage = reviewRepository.findAll(pageable);

        return reviewPage.map(r -> ReviewListDTO.builder()
                .id(r.getId())
                .title(r.getTitle())
                .authorName(r.getAuthor().getUsername())
                .rating(r.getRating())
                .category(r.getCategory())
                .createdAt(r.getCreatedAt())
                .likeCount(reviewLikeRepository.countByReviewId(r.getId()))
                .build());
    }

    public Review getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));
    }

    public void deleteReview(Long id, User user) {
        Review review = getReview(id);
        if (!review.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }
        reviewRepository.delete(review);
    }

    public Page<ReviewListDTO> getFilteredReviews(
            int page, int size, String sort, String category, User user) {

        Sort sorting = switch (sort) {
            case "ratingDesc" -> Sort.by(Sort.Direction.DESC, "rating");
            case "ratingAsc" -> Sort.by(Sort.Direction.ASC, "rating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Review> reviews;
        if (user != null && category != null) {
            reviews = reviewRepository.findByAuthorAndCategory(user, category, pageable);
        } else if (user != null) {
            reviews = reviewRepository.findByAuthor(user, pageable);
        } else if (category != null) {
            reviews = reviewRepository.findByCategory(category, pageable);
        } else {
            reviews = reviewRepository.findAll(pageable);
        }

        return reviews.map(r -> ReviewListDTO.builder()
                .id(r.getId())
                .title(r.getTitle())
                .authorName(r.getAuthor().getUsername())
                .rating(r.getRating())
                .category(r.getCategory())
                .createdAt(r.getCreatedAt())
                .likeCount(reviewLikeRepository.countByReviewId(r.getId()))
                .build());
    }

    public Review updateReview(Long id, String title, String content,
                               int rating, MultipartFile image, User user) {
        Review review = getReview(id);

        if (!review.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        review.setTitle(title);
        review.setContent(content);
        review.setRating(rating);

        if (image != null && !image.isEmpty()) {
            review.setImageUrl(saveImage(image));
        }

        return reviewRepository.save(review);
    }

    private String saveImage(MultipartFile image) {
        if (image == null || image.isEmpty()) return null;

        try {
            if (!Files.exists(uploadDir)) Files.createDirectories(uploadDir);
            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path path = uploadDir.resolve(filename);
            Files.copy(image.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/reviews/" + filename; // URL로 접근 가능
        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패: " + e.getMessage());
        }
    }
}