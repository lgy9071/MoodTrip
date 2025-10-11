package com.example.boardTest.entity.board;

import com.example.boardTest.entity.Base;
import com.example.boardTest.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "posts")
public class Post extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=200)
    private String title;

    @Lob @Column(nullable=false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Column(length=500)
    private String imageUrl;

    // 타입(영화/드라마/기타), 플랫폼(Netflix), 감상일, 태그
    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private ContentType type;         // MOVIE, SERIES, etc..

    @Column(length=50)
    private String platform;          // "Netflix", "TVING", "Disney+", ...

    private LocalDate watchedAt;      // 감상일

    @Column(length=255)
    private String tags;              // "넷플릭스,데이트,주말" (CSV)
}