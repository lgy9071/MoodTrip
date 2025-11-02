package com.example.boardTest.repository;

import com.example.boardTest.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {
    List<TripPlan> findByTitleContainingIgnoreCase(String keyword);
}