package com.example.boardTest.repository;

import com.example.boardTest.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {
    Page<Place> findByCategoryOrName(String cat, String name, Pageable p);
}