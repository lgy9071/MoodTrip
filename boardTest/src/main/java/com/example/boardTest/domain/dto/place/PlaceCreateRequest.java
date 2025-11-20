package com.example.boardTest.domain.dto.place;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceCreateRequest {
    private String name;
    private String category;
    private String address;
    private Integer rating;
    private String imageUrl;
    private String memo;
}