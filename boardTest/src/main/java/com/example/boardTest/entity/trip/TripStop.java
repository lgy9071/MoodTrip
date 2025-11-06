package com.example.boardTest.entity.trip;

import com.example.boardTest.domain.trip.TripCostCategory;
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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="trip_id")
    private TripPlan trip;

    private Integer dayOrder;         // n일차

    @Column(length=150)
    private String placeName;         // 방문 장소명

    @Column(length=200)
    private String address;           // 선택

    @Lob
    private String memo;              // 메모(추천 메뉴, 시간 등)

    @Column(precision = 12, scale = 2) // 9,999,999,999.99까지
    private BigDecimal cost;           // 해당 경유지 비용(원화 기준 등)

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private TripCostCategory category = TripCostCategory.OTHER;
}