package com.example.boardTest.repository;

import com.example.boardTest.entity.Place;
import com.example.boardTest.entity.PlaceFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaceFavoriteRepository extends JpaRepository<PlaceFavorite, Long> {

    Optional<PlaceFavorite> findByUserIdAndPlaceId(Long userId, Long placeId);

    long countByPlaceId(Long placeId);

    @Query("""
      select p from Place p 
      join PlaceFavorite f on f.place.id = p.id
      where f.user.id = :userId
    """)
    Page<Place> findFavoritesByUserId(@Param("userId") Long userId, Pageable pageable);
}