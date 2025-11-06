package com.example.boardTest.entity.review;

import com.example.boardTest.entity.Base;
import com.example.boardTest.entity.User;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String title;

    @Lob
    @Column(nullable=false)
    private String content;

    private int rating;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="author_id")
    private User author;

    // 어떤 종류의 리뷰인지 구분 (영화 / 여행 / 맛집)
    @Column(nullable = false)
    private String category; // "movie", "trip", "place"

    // 리뷰 대상의 id (Post, Trip, Place 등)
    @Column(nullable = false)
    private Long targetId;

    private String imageUrl;
}
