package com.example.boardTest.repository;

import com.example.boardTest.entity.Place;
import com.example.boardTest.entity.PlaceFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PlaceFavoriteRepository extends JpaRepository<PlaceFavorite, Long> {

    Optional<PlaceFavorite> findByUserIdAndPlaceId(Long userId, Long placeId);

    long countByPlaceId(Long placeId);

    @Query("""
      select p from Place p 
      join PlaceFavorite f on f.place.id = p.id
      where f.user.id = :userId
    """)
    Page<Place> findFavoritesByUserId(@Param("userId") Long userId, Pageable pageable);

    // 목록의 placeIds에 대해 내가 즐겨찾기한 항목들 한 번에 조회
    @Query("select f.place.id from PlaceFavorite f where f.user.id = :userId and f.place.id in :placeIds")
    List<Long> findMyFavoritedPlaceIdsIn(@Param("userId") Long userId, @Param("placeIds") Collection<Long> placeIds);

    // 목록의 placeIds별 즐겨찾기 개수 일괄 집계
    @Query("select f.place.id, count(f) from PlaceFavorite f where f.place.id in :placeIds group by f.place.id")
    List<Object[]> countByPlaceIdIn(@Param("placeIds") Collection<Long> placeIds);
}