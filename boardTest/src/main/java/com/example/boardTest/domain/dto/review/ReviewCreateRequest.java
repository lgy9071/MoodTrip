package com.example.boardTest.domain.dto.review;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {

    // 리뷰 제목
    private String title;

    // 리뷰 내용
    private String content;

    // 평점
    private int rating;

    // 리뷰 대상 카테고리
    // - movie / trip / place
    private String category;

    // 대상 ID
    // - 영화 ID / 여행 ID / 장소 ID
    private Long targetId;

    // 업로드 이미지
    // - MultipartFile 그대로 전달
    private MultipartFile image;
}
