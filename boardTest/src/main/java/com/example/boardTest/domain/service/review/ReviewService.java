package com.example.boardTest.domain.service.review;

import com.example.boardTest.domain.dto.review.ReviewListDTO;
import com.example.boardTest.domain.entity.review.Review;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.repository.review.ReviewLikeRepository;
import com.example.boardTest.domain.repository.review.ReviewRepository;
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

    // 리뷰 엔티티 Repository
    private final ReviewRepository reviewRepository;

    // 리뷰 좋아요 집계용 Repository
    private final ReviewLikeRepository reviewLikeRepository;

    // 리뷰 이미지 저장 경로
    private final Path uploadDir = Paths.get("uploads/reviews"); // 저장 폴더

    /**
     * 리뷰 생성
     * - 이미지 업로드 처리 포함
     */
    public Review saveReview(String title,
                             String content,
                             int rating,
                             User author,
                             String category,
                             Long targetId,
                             MultipartFile image) {

        // 이미지 저장 처리 (없으면 null)
        String imagePath = saveImage(image);

        // 리뷰 엔티티 생성
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

    /**
     * 리뷰 목록 조회 (기본)
     * - 최신순 정렬
     * - Review → ReviewListDTO 변환
     */
    public Page<ReviewListDTO> getReviews(int page, int size) {

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Review> reviewPage =
                reviewRepository.findAll(pageable);

        // Entity → DTO 매핑
        return reviewPage.map(r ->
                ReviewListDTO.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .authorName(r.getAuthor().getUsername())
                        .rating(r.getRating())
                        .category(r.getCategory())
                        .createdAt(r.getCreatedAt())
                        .likeCount(
                                reviewLikeRepository.countByReviewId(r.getId())
                        )
                        .build()
        );
    }

    /**
     * 리뷰 단건 조회
     */
    public Review getReview(Long id) {
        return reviewRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("리뷰가 존재하지 않습니다.")
                );
    }

    /**
     * 리뷰 삭제
     * - 작성자 본인만 가능
     */
    public void deleteReview(Long id, User user) {

        Review review = getReview(id);

        // 작성자 검증
        if (!review.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    /**
     * 리뷰 목록 필터 조회
     * - 정렬 방식
     * - 카테고리
     * - 내 리뷰 여부
     */
    public Page<ReviewListDTO> getFilteredReviews(
            int page,
            int size,
            String sort,
            String category,
            User user) {

        // 정렬 기준 결정
        Sort sorting = switch (sort) {
            case "ratingDesc" -> Sort.by(Sort.Direction.DESC, "rating");
            case "ratingAsc" -> Sort.by(Sort.Direction.ASC, "rating");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Review> reviews;

        // 사용자 + 카테고리 필터
        if (user != null && category != null) {
            reviews = reviewRepository.findByAuthorAndCategory(
                    user, category, pageable
            );
        }
        // 사용자만 필터
        else if (user != null) {
            reviews = reviewRepository.findByAuthor(user, pageable);
        }
        // 카테고리만 필터
        else if (category != null) {
            reviews = reviewRepository.findByCategory(category, pageable);
        }
        // 전체 조회
        else {
            reviews = reviewRepository.findAll(pageable);
        }

        // Entity → DTO 변환
        return reviews.map(r ->
                ReviewListDTO.builder()
                        .id(r.getId())
                        .title(r.getTitle())
                        .authorName(r.getAuthor().getUsername())
                        .rating(r.getRating())
                        .category(r.getCategory())
                        .createdAt(r.getCreatedAt())
                        .likeCount(
                                reviewLikeRepository.countByReviewId(r.getId())
                        )
                        .build()
        );
    }

    /**
     * 리뷰 수정
     * - 작성자 검증
     * - 이미지 변경 시 재업로드
     */
    public Review updateReview(Long id,
                               String title,
                               String content,
                               int rating,
                               MultipartFile image,
                               User user) {

        Review review = getReview(id);

        // 작성자 검증
        if (!review.getAuthor().getId().equals(user.getId())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        // 필드 수정
        review.setTitle(title);
        review.setContent(content);
        review.setRating(rating);

        // 이미지 변경이 있는 경우만 저장
        if (image != null && !image.isEmpty()) {
            review.setImageUrl(saveImage(image));
        }

        return reviewRepository.save(review);
    }

    /**
     * 이미지 저장 공통 메서드
     * - 파일 시스템에 저장
     * - 저장된 경로를 URL 형태로 반환
     */
    private String saveImage(MultipartFile image) {

        if (image == null || image.isEmpty()) return null;

        try {
            // 디렉터리 없으면 생성
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            // 파일명 중복 방지를 위한 UUID 사용
            String filename =
                    UUID.randomUUID() + "_" + image.getOriginalFilename();

            Path path = uploadDir.resolve(filename);

            // 파일 복사
            Files.copy(
                    image.getInputStream(),
                    path,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // 웹 접근 경로 반환
            return "/uploads/reviews/" + filename;

        } catch (IOException e) {
            throw new RuntimeException(
                    "이미지 저장 실패: " + e.getMessage()
            );
        }
    }
}