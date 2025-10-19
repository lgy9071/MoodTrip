package com.example.boardTest.repository;

import com.example.boardTest.entity.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {
    List<TripStop> findByTripIdOrderByDayOrderAsc(Long tripId);
    void deleteByTripId(Long tripId);

    @Query("select coalesce(sum(s.cost),0) from TripStop s where s.trip.id=:tripId")
    BigDecimal sumCostByTripId(@Param("tripId") Long tripId);

    @Query("select s.dayOrder, coalesce(sum(s.cost),0) from TripStop s where s.trip.id=:tripId group by s.dayOrder order by s.dayOrder")
    List<Object[]> sumCostByDay(@Param("tripId") Long tripId);
}