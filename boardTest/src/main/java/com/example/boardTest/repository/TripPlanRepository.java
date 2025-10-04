package com.example.boardTest.repository;

import com.example.boardTest.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {
}