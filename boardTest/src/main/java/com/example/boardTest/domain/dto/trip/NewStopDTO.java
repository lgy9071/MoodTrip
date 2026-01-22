package com.example.boardTest.domain.dto.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewStopDTO {

        // 여행 일차 (1일차, 2일차 ...)
        @NotNull(message = "일차를 입력하세요.")
        private Integer dayOrder;

        // 장소명
        @NotBlank(message = "장소명을 입력하세요.")
        private String placeName;

        // 주소 (선택)
        private String address;

        // 메모
        private String memo;

        // 비용
        // - 소수점 2자리까지 허용
        @NotNull
        @Digits(integer = 10, fraction = 2)
        @PositiveOrZero
        private BigDecimal cost;

        // 비용 분류 (교통/숙박/식사 등)
        @NotNull
        private TripCostCategory category;

        // 업로드 이미지
        private MultipartFile image;
}
