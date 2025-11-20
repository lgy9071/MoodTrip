package com.example.boardTest.domain.dto.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;

import java.math.BigDecimal;

// 응답 DTO: 폼의 NewStopDTO와 호환되게 구성
public record SuggestedStopDTO(
        Integer dayOrder,
        String placeName,
        String address,
        String memo,
        BigDecimal cost,
        TripCostCategory category
) {}