package com.example.boardTest.dto.trip;

import com.example.boardTest.domain.trip.TripCostCategory;
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

        @NotNull(message = "일차를 입력하세요.")
        private Integer dayOrder;

        @NotBlank(message = "장소명을 입력하세요.")
        private String placeName;

        private String address;
        private String memo;

        @NotNull
        @Digits(integer = 10, fraction = 2)
        @PositiveOrZero
        private BigDecimal cost;

        @NotNull
        private TripCostCategory category;

        private MultipartFile image;

}
