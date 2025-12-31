package com.example.boardTest.domain.service.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;
import com.example.boardTest.domain.dto.trip.TripStopCreateDTO;
import com.example.boardTest.domain.dto.trip.TripSummaryDTO;
import com.example.boardTest.domain.entity.trip.TripPlan;
import com.example.boardTest.domain.entity.trip.TripStop;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.repository.trip.TripPlanRepository;
import com.example.boardTest.domain.repository.trip.TripStopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 여행 계획 / 경유지 비즈니스 로직 처리 Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TripService {

    private final TripPlanRepository planRepo;
    private final TripStopRepository stopRepo;

    /**
     * 업로드 루트 디렉토리
     */
    private final Path root = Paths.get("uploads");

    /**
     * 여행 계획 목록 (페이징)
     */
    public Page<TripPlan> listPlans(int page, int size) {
        page = Math.max(0, page);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "id")
        );

        return planRepo.findAll(pageable);
    }

    /**
     * 여행 계획 생성
     */
    public TripPlan createPlan(String title,
                               LocalDate start,
                               LocalDate end,
                               User owner) {

        TripPlan plan = TripPlan.builder()
                .title(title)
                .startDate(start)
                .endDate(end)
                .owner(owner)
                .build();

        return planRepo.save(plan);
    }

    /**
     * 여행 계획 단건 조회
     */
    public TripPlan findPlan(Long id) {
        return planRepo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("여행 계획을 찾을 수 없습니다."));
    }

    /**
     * 여행 계획 삭제 (작성자 검증)
     */
    public void deletePlan(Long id, User requester) {
        TripPlan plan = findPlan(id);

        if (!plan.getOwner().getId().equals(requester.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        planRepo.delete(plan);
    }

    /**
     * 특정 여행의 경유지 목록 조회
     */
    public List<TripStop> getStops(Long planId) {
        return stopRepo.findByTripIdOrderByDayOrderAsc(planId);
    }

    /**
     * 경유지 추가
     */
    public TripStop addStop(Long planId,
                            TripStopCreateDTO dto,
                            User requester) {

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
                .cost(dto.cost())
                .category(dto.category())
                .imageUrl(dto.imageUrl())
                .build();

        return stopRepo.save(stop);
    }

    /**
     * 경유지 삭제
     */
    public void removeStop(Long stopId) {
        stopRepo.deleteById(stopId);
    }

    /**
     * 경유지 일차 변경
     */
    public void moveDay(Long stopId, int newDay) {
        TripStop stop = stopRepo.findById(stopId)
                .orElseThrow(() ->
                        new IllegalArgumentException("일정 항목이 없습니다."));

        // Dirty Checking으로 자동 업데이트
        stop.setDayOrder(newDay);
    }

    /**
     * 전체 비용 합계
     */
    public BigDecimal totalCost(Long planId) {
        return stopRepo.sumCostByTripId(planId);
    }

    /**
     * 일차별 비용 합계
     */
    public Map<Integer, BigDecimal> dayCostMap(Long planId) {
        Map<Integer, BigDecimal> map = new LinkedHashMap<>();

        for (Object[] row : stopRepo.sumCostByDay(planId)) {
            Number day = (Number) row[0];
            BigDecimal sum =
                    (row[1] != null) ? (BigDecimal) row[1] : BigDecimal.ZERO;

            map.put(day.intValue(), sum);
        }

        return map;
    }

    /**
     * 카테고리별 비용 합계
     */
    public Map<TripCostCategory, BigDecimal> categoryCostMap(Long planId) {
        Map<TripCostCategory, BigDecimal> map =
                new EnumMap<>(TripCostCategory.class);

        for (Object[] row : stopRepo.sumCostByCategory(planId)) {
            TripCostCategory cat = (TripCostCategory) row[0];
            BigDecimal sum =
                    (row[1] != null) ? (BigDecimal) row[1] : BigDecimal.ZERO;

            map.put(cat, sum);
        }

        return map;
    }

    /**
     * 리뷰/목록용 요약 DTO 조회
     */
    public List<TripSummaryDTO> findAllPlans() {
        List<TripPlan> plans =
                planRepo.findAll(Sort.by(Sort.Direction.DESC, "id"));

        return plans.stream()
                .map(TripSummaryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 제목 검색
     */
    public List<TripPlan> search(String keyword) {
        return planRepo.findByTitleContainingIgnoreCase(keyword);
    }

    /**
     * 이미지 업로드 처리
     */
    public String saveTripImage(Long planId, MultipartFile file) {

        if (file == null || file.isEmpty()) return null;

        try {
            // uploads/trips/{planId}/
            Path dir = root.resolve("trips").resolve(String.valueOf(planId));
            Files.createDirectories(dir);

            String filename =
                    System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path dest = dir.resolve(filename).normalize();

            try (InputStream in = file.getInputStream()) {
                Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
            }

            // 정적 리소스 URL
            return "/uploads/trips/" + planId + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("이미지 저장 실패", e);
        }
    }
}
