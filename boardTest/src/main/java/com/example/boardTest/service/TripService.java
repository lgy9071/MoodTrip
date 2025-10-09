package com.example.boardTest.service;

import com.example.boardTest.dto.TripStopCreateDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripPlanRepository planRepo;
    private final TripStopRepository stopRepo;

    // Plan
    public Page<TripPlan> listPlans(int page, int size) {
        page = Math.max(0, page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        return planRepo.findAll(pageable); // 자동으로 Page<TripPlan>
    }

    public TripPlan createPlan(String title, LocalDate start, LocalDate end, User owner) {
        TripPlan plan = TripPlan.builder()
                .title(title)
                .startDate(start)
                .endDate(end)
                .owner(owner)
                .build();
        return planRepo.save(plan);
    }

    public TripPlan findPlan(Long id) {
        return planRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));
    }

    public void deletePlan(Long id, User requester) {
        TripPlan plan = findPlan(id);
        if (!plan.getOwner().getId().equals(requester.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }
        planRepo.delete(plan);
    }

    // Stop
    public List<TripStop> getStops(Long planId) {
        return stopRepo.findByTripIdOrderByDayOrderAsc(planId);
    }

    public TripStop addStop(Long planId, TripStopCreateDTO dto, User requester) {
        TripPlan plan = findPlan(planId);
        if (!plan.getOwner().getId().equals(requester.getId())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }
        TripStop stop = TripStop.builder()
                .trip(plan)
                .dayOrder(dto.dayOrder())
                .placeName(dto.placeName())
                .address(dto.address())
                .memo(dto.memo())
                .build();
        return stopRepo.save(stop);
    }

    public void removeStop(Long stopId) {
        stopRepo.deleteById(stopId);
    }

    public void moveDay(Long stopId, int newDay) {
        TripStop stop = stopRepo.findById(stopId)
                .orElseThrow(() -> new IllegalArgumentException("일정 항목이 없습니다."));
        stop.setDayOrder(newDay);
        // dirty checking으로 업데이트
    }
}