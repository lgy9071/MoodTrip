package com.example.boardTest.service;

import com.example.boardTest.entity.TripPlan;
import com.example.boardTest.entity.TripStop;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.TripPlanRepository;
import com.example.boardTest.repository.TripStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
class TripService {
    private final TripPlanRepository planRepo;
    private final TripStopRepository stopRepo;

    // Plan
    public Page<TripPlan> listPlans(int page, int size) {
        page = Math.max(0, page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return planRepo.findAll(pageable);
    }

    public TripPlan createPlan(String title, LocalDate start, LocalDate end, User owner) {
        TripPlan plan = TripPlan.builder().title(title).startDate(start).endDate(end).owner(owner).build();
        return planRepo.save(plan);
    }

    public TripPlan findPlan(Long id) {
        return planRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));
    }

    public void deletePlan(Long id, User requester) {
        TripPlan plan = findPlan(id);
        if (!plan.getOwner().getId().equals(requester.getId())) throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        // 먼저 스탑들 삭제
        List<TripStop> stops = stopRepo.findByTripIdOrderByDayOrderAsc(id);
        stopRepo.deleteAll(stops);
        planRepo.delete(plan);
    }

    // Stop
    public List<TripStop> stopsOf(Long planId) {
        return stopRepo.findByTripIdOrderByDayOrderAsc(planId);
    }

    public TripStop addStop(Long planId, Integer dayOrder, String placeName, String address, String memo, User requester) {
        TripPlan plan = findPlan(planId);
        if (!plan.getOwner().getId().equals(requester.getId())) throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        TripStop stop = TripStop.builder().trip(plan).dayOrder(dayOrder).placeName(placeName).address(address).memo(memo).build();
        return stopRepo.save(stop);
    }

    public void deleteStop(Long stopId, User requester) {
        TripStop stop = stopRepo.findById(stopId).orElseThrow(() -> new IllegalArgumentException("일정 항목이 없습니다."));
        if (!stop.getTrip().getOwner().getId().equals(requester.getId())) throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        stopRepo.delete(stop);
    }
}
