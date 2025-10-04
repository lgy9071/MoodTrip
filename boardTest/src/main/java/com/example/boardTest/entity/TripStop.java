package com.example.boardTest.entity;

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
}