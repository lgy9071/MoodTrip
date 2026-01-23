package com.example.boardTest.domain.entity.review;

import com.example.boardTest.global.utils.Base;
import com.example.boardTest.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "reviews")
public class Review extends Base {

    // 리뷰 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리뷰 제목
    @Column(nullable=false)
    private String title;

    // 리뷰 본문
    // - 긴 텍스트 대응
    @Lob
    @Column(nullable=false)
    private String content;

    // 평점
    private int rating;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="author_id")
    private User author;

    // 리뷰 대상 카테고리
    // - movie / trip / place
    @Column(nullable = false)
    private String category;

    // 대상 엔티티 ID
    // - Post / Trip / Place 등 공용 참조
    @Column(nullable = false)
    private Long targetId;

    // 리뷰 이미지 URL
    private String imageUrl;
}
