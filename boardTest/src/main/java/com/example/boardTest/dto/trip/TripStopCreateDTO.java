package com.example.boardTest.dto.trip;

import com.example.boardTest.domain.trip.TripCostCategory;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

// record를 계속 쓰고 싶다면 아래처럼 패키지/임포트만 정리해서 그대로 사용
public record TripStopCreateDTO(
        @NotNull(message = "일차를 입력하세요.")
        Integer dayOrder,

        @NotBlank(message = "장소명을 입력하세요.")
        String placeName,

        String address,
        String memo,

        @NotNull(message = "비용을 입력하세요.")
        @Digits(integer=10, fraction=2)
        @PositiveOrZero
        BigDecimal cost,

        @NotNull(message = "분류를 선택하세요.")
        TripCostCategory category
) {}