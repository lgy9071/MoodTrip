package com.example.boardTest.domain.entity.trip;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="trip_stops")
public class TripStop {

    // 경유지 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 소속 여행
    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="trip_id")
    private TripPlan trip;

    // 몇 일차인지
    private Integer dayOrder;

    // 장소명
    @Column(length=150)
    private String placeName;

    // 주소 (선택)
    @Column(length=200)
    private String address;

    // 메모
    @Lob
    private String memo;

    // 비용
    // - 최대 9,999,999,999.99
    @Column(precision = 12, scale = 2)
    private BigDecimal cost;

    // 비용 분류
    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private TripCostCategory category = TripCostCategory.OTHER;

    // 이미지 URL
    @Column(length=300)
    private String imageUrl;
}
