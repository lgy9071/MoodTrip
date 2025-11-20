package com.example.boardTest.domain.dto.trip;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

// 요청 DTO: 새 계획 화면에서 기간/도시/테마/예산을 보내줌
@Data
public class StopSuggestRequest {
    @NotNull
    private LocalDate startDate;
    @NotNull private LocalDate endDate;
    private String city;             // 예: Osaka, Tokyo ...
    private String theme;            // 예: food, night-view, history ...
    private Integer budgetPerDay;    // 원(₩) 단위, 선택
}