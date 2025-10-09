package com.example.boardTest.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class TripPlanCreateDTO {

    @NotBlank(message = "제목을 입력하세요.")
    private String title;

    @NotNull(message = "시작일을 선택하세요.")
    private LocalDate startDate;

    @NotNull(message = "종료일을 선택하세요.")
    private LocalDate endDate;

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

    public String getTitle() { return title; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }

    public void setTitle(String title) { this.title = title; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}