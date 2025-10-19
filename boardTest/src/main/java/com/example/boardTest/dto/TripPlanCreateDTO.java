package com.example.boardTest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TripPlanCreateDTO {

    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotNull(message = "시작일을 선택하세요.")
    private LocalDate startDate;

    @NotNull(message = "종료일을 선택하세요.")
    private LocalDate endDate;

    // --- 초기 경유지 ---
    private Integer initialDayOrder;     // null 이면 입력 안 한 것
    private String initialPlaceName;
    private String initialAddress;
    private String initialMemo;
    private BigDecimal initialCost;

    public TripPlanCreateDTO() {}

    public TripPlanCreateDTO(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // 간단한 도메인 검증(시작<=종료)
    public boolean isDateRangeValid() {
        return startDate != null && endDate != null && !endDate.isBefore(startDate);
    }
}