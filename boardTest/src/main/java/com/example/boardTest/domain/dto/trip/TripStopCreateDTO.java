package com.example.boardTest.domain.dto.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

// record를 계속 쓰고 싶다면 아래처럼 패키지/임포트만 정리해서 그대로 사용
public record TripStopCreateDTO(

        // 일차
        @NotNull(message = "일차를 입력하세요.")
        Integer dayOrder,

        // 장소명
        @NotBlank(message = "장소명을 입력하세요.")
        String placeName,

        // 주소
        String address,

        // 메모
        String memo,

        // 비용
        @NotNull(message = "비용을 입력하세요.")
        @Digits(integer=10, fraction=2)
        @PositiveOrZero
        BigDecimal cost,

        // 비용 분류
        @NotNull(message = "분류를 선택하세요.")
        TripCostCategory category,

        // 저장된 이미지 URL
        String imageUrl,

        // 업로드 파일
        MultipartFile image
) {}