package com.example.boardTest.domain.repository.place;

import com.example.boardTest.domain.entity.place.Place;
import com.example.boardTest.domain.entity.place.PlaceFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlaceFavoriteRepository extends JpaRepository<PlaceFavorite, Long> {

    /**
     * 특정 사용자가 특정 장소를 즐겨찾기 했는지 조회
     * - 즐겨찾기 토글 시 존재 여부 판단용
     */
    Optional<PlaceFavorite> findByUserIdAndPlaceId(Long userId, Long placeId);

    /**
     * 특정 장소의 즐겨찾기 개수
     * - 장소 상세 화면에서 카운트 표시용
     */
    long countByPlaceId(Long placeId);

    /**
     * 특정 사용자가 즐겨찾기한 장소 목록 조회 (페이징)
     * - PlaceFavorite → Place 조인
     * - 즐겨찾기 화면 전용 쿼리
     */
    @Query("""
      select p from Place p 
      join PlaceFavorite f on f.place.id = p.id
      where f.user.id = :userId
    """)
    Page<Place> findFavoritesByUserId(@Param("userId") Long userId, Pageable pageable);
}
