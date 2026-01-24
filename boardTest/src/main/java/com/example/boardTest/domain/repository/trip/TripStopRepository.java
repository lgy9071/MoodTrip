package com.example.boardTest.domain.repository.trip;

import com.example.boardTest.domain.entity.trip.TripStop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface TripStopRepository extends JpaRepository<TripStop, Long> {

    /**
     * 특정 여행의 경유지 목록
     * - 일차(dayOrder) 기준 정렬
     */
    List<TripStop> findByTripIdOrderByDayOrderAsc(Long tripId);

    /**
     * 특정 여행의 모든 경유지 삭제
     * - 여행 삭제 시 사용
     */
    void deleteByTripId(Long tripId);

    /**
     * 여행 전체 비용 합계
     */
    @Query("select coalesce(sum(s.cost),0) from TripStop s where s.trip.id=:tripId")
    BigDecimal sumCostByTripId(@Param("tripId") Long tripId);

    /**
     * 일차별 비용 합계
     * - 결과: [dayOrder, sum(cost)]
     * - 차트/통계용
     */
    @Query("""
        select s.dayOrder, coalesce(sum(s.cost),0)
        from TripStop s
        where s.trip.id=:tripId
        group by s.dayOrder
        order by s.dayOrder
    """)
    List<Object[]> sumCostByDay(@Param("tripId") Long tripId);

    /**
     * 카테고리별 비용 합계
     * - 교통/숙박/식사 등 집계
     */
    @Query("""
        select s.category, coalesce(sum(s.cost),0)
        from TripStop s
        where s.trip.id=:tripId
        group by s.category
    """)
    List<Object[]> sumCostByCategory(@Param("tripId") Long tripId);
}
