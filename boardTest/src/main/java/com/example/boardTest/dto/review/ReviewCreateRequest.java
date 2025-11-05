package com.example.boardTest.dto.review;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {
    private String title;
    private String content;
    private int rating;
    private String category;
    private Long targetId;
    private MultipartFile image;
}