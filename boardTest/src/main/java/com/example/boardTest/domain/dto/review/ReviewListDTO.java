package com.example.boardTest.domain.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewListDTO {
    private Long id;
    private String title;
    private String authorName;
    private int rating;
    private String category;
    private LocalDateTime createdAt;
    private long likeCount;
}