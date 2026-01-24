package com.example.boardTest.domain.repository.place;

import com.example.boardTest.domain.entity.place.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {

    /**
     * 카테고리 또는 장소명 부분 검색 (대소문자 무시)
     * - 페이징 지원
     * - 목록 화면 검색 기능
     */
    Page<Place> findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCase(
            String category,
            String name,
            Pageable p
    );

    /**
     * 장소명 또는 카테고리 부분 검색
     * - 자동완성, 검색 API 등에서 사용
     */
    List<Place> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name,
            String category
    );
}
