package com.example.boardTest.repository;

import com.example.boardTest.entity.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {
    List<TripStop> findByTripIdOrderByDayOrderAsc(Long tripId);
    void deleteByTripId(Long tripId);
}