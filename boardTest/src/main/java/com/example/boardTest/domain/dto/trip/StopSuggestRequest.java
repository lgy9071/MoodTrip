package com.example.boardTest.domain.dto.trip;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

// 요청 DTO: 새 계획 화면에서 기간/도시/테마/예산을 보내줌
@Data
public class StopSuggestRequest {

    // 여행 시작일
    @NotNull
    private LocalDate startDate;

    // 여행 종료일
    @NotNull
    private LocalDate endDate;

    // 도시명 (예: Osaka, Tokyo)
    private String city;

    // 여행 테마 (food, night-view 등)
    private String theme;

    // 하루 예산 (선택)
    private Integer budgetPerDay;
}
