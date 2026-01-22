package com.example.boardTest.domain.dto.review;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequest {

    // 수정할 제목
    private String title;

    // 수정할 내용
    private String content;

    // 수정할 평점
    private int rating;

    // 수정 이미지 (선택)
    private MultipartFile image;
}