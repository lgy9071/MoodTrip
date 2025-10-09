package com.example.boardTest.repository;

import com.example.boardTest.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    // 부분 일치/대소문자 무시
    Page<Place> findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCase(String category, String name, Pageable p);
}