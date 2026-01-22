package com.example.boardTest.domain.dto.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor   // 기본 생성자만 쓰자 (권장)
public class TripPlanCreateDTO {

    // 여행 제목
    @NotBlank
    private String title;

    // 시작일
    @NotNull
    private LocalDate startDate;

    // 종료일
    @NotNull
    private LocalDate endDate;

    // (이전 단일 경유지 입력용 필드들 - 호환용 유지)
    private Integer initialDayOrder;
    private String initialPlaceName;
    private String initialAddress;
    private String initialMemo;
    private BigDecimal initialCost;

    // 다중 경유지 리스트
    // - 항상 null이 아니도록 관리
    private List<NewStopDTO> stops = new ArrayList<>();

    /**
     * 날짜 범위 유효성 체크
     */
    public boolean isDateRangeValid() {
        return startDate != null && endDate != null
                && !endDate.isBefore(startDate);
    }

    /**
     * 초기 단일 경유지 존재 여부
     */
    public boolean hasInitialStop() {
        return initialPlaceName != null && !initialPlaceName.isBlank();
    }

    /**
     * 모든 경유지의 일차(dayOrder)가 여행 기간 내인지 검증
     */
    @AssertTrue(message = "경유지의 '일차'는 선택한 여행 기간 범위를 벗어날 수 없습니다.")
    public boolean isStopsWithinRange() {

        if (startDate == null || endDate == null || stops == null) return true;

        long maxDays =
                ChronoUnit.DAYS.between(startDate, endDate) + 1;

        for (NewStopDTO s : stops) {
            if (s == null ||
                    s.getPlaceName() == null ||
                    s.getPlaceName().isBlank()) continue;

            if (s.getDayOrder() == null ||
                    s.getDayOrder() < 1 ||
                    s.getDayOrder() > maxDays)
                return false;
        }
        return true;
    }

    /**
     * stops 리스트 초기화
     * - Thymeleaf 렌더링 에러 방지
     */
    public void initIfEmpty() {
        if (this.stops == null) {
            this.stops = new ArrayList<>();
        }
        if (this.stops.isEmpty()) {
            this.stops.add(
                    new NewStopDTO(
                            1,
                            "",
                            null,
                            null,
                            BigDecimal.ZERO,
                            TripCostCategory.OTHER,
                            null
                    )
            );
        }
    }
}
