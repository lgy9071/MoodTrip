package com.example.boardTest.dto.place;

import com.example.boardTest.entity.Place;
import lombok.*;

// 출력용 (view/응답)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceDTO {
    private Long id;
    private String name;
    private String category;
    private String address;
    private Integer rating;

    public static PlaceDTO fromEntity(Place place) {
        return PlaceDTO.builder()
                .id(place.getId())
                .name(place.getName())
                .category(place.getCategory())
                .address(place.getAddress())
                .rating(place.getRating())
                .build();
    }
}