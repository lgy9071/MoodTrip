package com.example.boardTest.repository;

import com.example.boardTest.entity.TripPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripPlanRepository extends JpaRepository<TripPlan, Long> {
}