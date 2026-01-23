package com.example.boardTest.domain.entity.trip;

import com.example.boardTest.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="trip_plans")
@EntityListeners(AuditingEntityListener.class)
public class TripPlan {

    // 여행 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 여행 제목
    @Column(nullable=false, length=120)
    private String title;

    // 여행 기간
    private LocalDate startDate;
    private LocalDate endDate;

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="owner_id")
    private User owner;

    /**
     * 여행 경유지 목록
     * - TripPlan이 삭제되면 TripStop도 함께 삭제
     * - orphanRemoval = true → 고아 객체 자동 제거
     */
    @OneToMany(
            mappedBy = "trip",
            cascade = CascadeType.REMOVE,
            orphanRemoval = true
    )
    @Builder.Default
    private List<TripStop> stops = new ArrayList<>();

    // 생성 시각 (자동)
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // 썸네일 이미지 URL
    String thumbnailUrl;
}
