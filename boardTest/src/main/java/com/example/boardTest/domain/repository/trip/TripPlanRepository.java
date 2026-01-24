package com.example.boardTest.domain.repository.trip;

import com.example.boardTest.domain.entity.trip.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {

    /**
     * 여행 제목 기준 검색
     * - 여행 검색 / 자동완성
     */
    List<TripPlan> findByTitleContainingIgnoreCase(String keyword);
}
