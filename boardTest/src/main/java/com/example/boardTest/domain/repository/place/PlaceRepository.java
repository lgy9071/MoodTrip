package com.example.boardTest.domain.repository.place;

import com.example.boardTest.domain.entity.place.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    // 부분 일치/대소문자 무시
    Page<Place> findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCase(String category, String name, Pageable p);

    List<Place> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(String name, String category);
}