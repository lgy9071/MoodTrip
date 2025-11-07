package com.example.boardTest.dto.trip;

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

    @NotBlank
    private String title;
    @NotNull
    private LocalDate startDate;
    @NotNull private LocalDate endDate;

    // (이전 단일 초기 경유지 필드들은 계속 놔둬도 되지만, 이제는 stops만 사용)
    private Integer initialDayOrder;
    private String initialPlaceName;
    private String initialAddress;
    private String initialMemo;
    private BigDecimal initialCost;

    // 항상 null 아니도록 기본값
    private List<NewStopDTO> stops = new ArrayList<>();

    public boolean isDateRangeValid() {
        return startDate != null && endDate != null && !endDate.isBefore(startDate);
    }
    public boolean hasInitialStop() {
        return initialPlaceName != null && !initialPlaceName.isBlank();
    }

    @AssertTrue(message = "경유지의 '일차'는 선택한 여행 기간 범위를 벗어날 수 없습니다.")
    public boolean isStopsWithinRange() {
        if (startDate == null || endDate == null || stops == null) return true;
        long maxDays = ChronoUnit.DAYS.between(startDate, endDate) + 1; // 양끝 포함
        for (NewStopDTO s : stops) {
            if (s == null || s.getPlaceName() == null || s.getPlaceName().isBlank()) continue; // 빈 행 스킵
            if (s.getDayOrder() == null || s.getDayOrder() < 1 || s.getDayOrder() > maxDays) return false;
        }
        return true;
    }
}
