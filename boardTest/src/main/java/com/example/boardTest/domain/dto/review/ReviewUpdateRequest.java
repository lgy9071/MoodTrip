package com.example.boardTest.domain.dto.review;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewUpdateRequest {
    private String title;
    private String content;
    private int rating;
    private MultipartFile image;
}