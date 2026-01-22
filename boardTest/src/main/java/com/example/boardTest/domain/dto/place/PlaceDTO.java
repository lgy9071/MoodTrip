package com.example.boardTest.domain.dto.place;

import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.entity.place.Place;
import lombok.*;

import java.time.LocalDateTime;

// 출력용 (view/응답)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlaceDTO {

    // 장소 ID
    private Long id;

    // 장소명
    private String name;

    // 카테고리
    private String category;

    // 주소
    private String address;

    // 평점
    private Integer rating;

    // 이미지 URL
    private String imageUrl;

    // 생성일시
    private LocalDateTime createdAt;

    // 작성자 (User Entity)
    // - View에서 작성자 이름 표시 등에 사용
    private User author;

    // 메모
    private String memo;

    /**
     * Entity → DTO 변환 메서드
     * - Controller에서 직접 new 하지 않도록 책임 집중
     */
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
