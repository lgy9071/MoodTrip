package com.example.boardTest.domain.dto.place;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceCreateRequest {

    // 장소명
    // - 필수 입력값
    // - View Form에서 name="name"으로 바인딩
    private String name;

    // 장소 카테고리
    // - 예: cafe, restaurant, sightseeing 등
    private String category;

    // 주소 (선택)
    private String address;

    // 평점
    // - 1~5 정수값 사용
    private Integer rating;

    // 이미지 URL
    // - 외부 이미지 링크 또는 업로드 후 저장된 경로
    private String imageUrl;

    // 메모
    // - 개인 기록용 자유 텍스트
    private String memo;
}
