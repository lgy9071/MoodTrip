package com.example.boardTest.dto.trip;

import com.example.boardTest.entity.Base;
import com.example.boardTest.entity.trip.TripPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripSummaryDTO extends Base {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;
    private String ownerName;

    public static TripSummaryDTO fromEntity(TripPlan plan) {
        return TripSummaryDTO.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .ownerName(plan.getOwner() != null ? plan.getOwner().getUsername() : "알 수 없음")
                .build();
    }
}