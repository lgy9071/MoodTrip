package com.example.boardTest.domain.entity.place;

import com.example.boardTest.global.utils.Base;
import com.example.boardTest.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "places")
public class Place extends Base {

    // 장소 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 장소명
    // - 필수
    // - 길이 제한으로 DB 인덱스 효율 고려
    @Column(nullable=false, length=120)
    private String name;

    // 주소 (선택)
    @Column(length=200)
    private String address;

    // 카테고리
    // - 문자열로 관리 (확장성 우선)
    // - 예: cafe, restaurant, sightseeing
    @Column(length=40)
    private String category;

    // 사용자 평점
    private Integer rating;

    // 이미지 URL (외부 링크 or 업로드 결과)
    @Column(length=500)
    private String imageUrl;

    // 자유 메모
    // - 길이 제한 없는 텍스트이므로 LOB 사용
    @Lob
    private String memo;

    // 작성자
    // - 다대일 관계
    // - 지연 로딩으로 성능 최적화
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="author_id")
    private User author;
}