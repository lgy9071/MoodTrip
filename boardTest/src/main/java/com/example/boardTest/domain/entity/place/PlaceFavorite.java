package com.example.boardTest.domain.entity.place;

import com.example.boardTest.domain.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "place_favorite",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_fav_user_place",
                columnNames = {"user_id","place_id"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceFavorite {

    // 즐겨찾기 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 즐겨찾기한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 즐겨찾기 대상 장소
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "place_id", nullable = false)
    private Place place;

    // 즐겨찾기 등록 시각
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티 저장 직전 자동 실행
     * - createdAt 값 자동 세팅
     */
    @PrePersist
    void prePersist() {
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }
}
