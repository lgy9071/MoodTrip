package com.example.boardTest.dto.place;

import com.example.boardTest.entity.User;
import com.example.boardTest.entity.place.Place;
import lombok.*;

import java.time.LocalDateTime;

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
    private String imageUrl;
    private LocalDateTime createdAt;
    private User author;
    private String memo;

    public static PlaceDTO fromEntity(Place place) {
        return PlaceDTO.builder()
                .id(place.getId())
                .name(place.getName())
                .category(place.getCategory())
                .address(place.getAddress())
                .rating(place.getRating())
                .imageUrl(place.getImageUrl())
                .createdAt(place.getCreatedAt())
                .author(place.getAuthor())
                .memo(place.getMemo())
                .build();
    }
}