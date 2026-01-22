package com.example.boardTest.domain.dto.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;

import java.math.BigDecimal;

// 응답 DTO: 폼의 NewStopDTO와 호환되게 구성
public record SuggestedStopDTO(

        // 일차
        Integer dayOrder,

        // 장소명
        String placeName,

        // 주소
        String address,

        // 메모
        String memo,

        // 예상 비용
        BigDecimal cost,

        // 비용 카테고리
        TripCostCategory category
) {}
