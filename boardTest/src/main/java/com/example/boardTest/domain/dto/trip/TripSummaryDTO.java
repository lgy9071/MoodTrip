package com.example.boardTest.domain.dto.trip;

import com.example.boardTest.global.utils.Base;
import com.example.boardTest.domain.entity.trip.TripPlan;
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

    // 여행 ID
    private Long id;

    // 여행 제목
    private String title;

    // 시작일
    private LocalDate startDate;

    // 종료일
    private LocalDate endDate;

    // 작성자 이름
    private String ownerName;

    /**
     * Entity → DTO 변환
     */
    public static TripSummaryDTO fromEntity(TripPlan plan) {
        return TripSummaryDTO.builder()
                .id(plan.getId())
                .title(plan.getTitle())
                .startDate(plan.getStartDate())
                .endDate(plan.getEndDate())
                .ownerName(
                        plan.getOwner() != null
                                ? plan.getOwner().getUsername()
                                : "알 수 없음"
                )
                .build();
    }
}