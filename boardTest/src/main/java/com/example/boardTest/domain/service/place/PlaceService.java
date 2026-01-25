package com.example.boardTest.domain.service.place;

import com.example.boardTest.domain.dto.place.PlaceDTO;
import com.example.boardTest.domain.entity.place.Place;
import com.example.boardTest.domain.entity.place.PlaceFavorite;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.repository.place.PlaceFavoriteRepository;
import com.example.boardTest.domain.repository.place.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaceService {

    // Place 엔티티 기본 CRUD 담당 Repository
    private final PlaceRepository repo;

    // Place 즐겨찾기(PlaceFavorite) 전용 Repository
    private final PlaceFavoriteRepository favRepo;

    /**
     * 장소 목록 조회
     * - keyword: 검색어 (카테고리 / 이름 부분 검색)
     * - page, size: 페이징 정보
     * - favoritesOfUserId: null이 아니면 해당 사용자의 즐겨찾기만 조회
     */
    public Page<Place> list(String keyword, int page, int size, Long favoritesOfUserId) {

        // page 값이 음수가 되는 것을 방지
        page = Math.max(0, page);

        // 생성일(createdAt) 기준 최신순 정렬
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        // 즐겨찾기 전용 조회 (userId가 전달된 경우)
        if (favoritesOfUserId != null) {
            return favRepo.findFavoritesByUserId(favoritesOfUserId, pageable);
        }

        // 검색어가 없는 경우 전체 조회
        if (keyword == null || keyword.isBlank()) {
            return repo.findAll(pageable);
        }

        // 카테고리 또는 장소명 기준 부분 검색
        return repo.findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCase(
                keyword,
                keyword,
                pageable
        );
    }

    /**
     * 장소 단건 조회
     */
    public Place find(Long id) {
        return repo.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("장소를 찾을 수 없습니다.")
                );
    }

    /**
     * 장소 생성
     * - 로그인한 사용자(author)를 작성자로 설정
     */
    public Place create(String name,
                        String address,
                        String category,
                        Integer rating,
                        String imageUrl,
                        String memo,
                        User author) {

        Place p = Place.builder()
                .name(name)
                .address(address)
                .category(category)
                .rating(rating)
                .imageUrl(imageUrl)
                .memo(memo)
                .author(author)
                .build();

        return repo.save(p);
    }

    /**
     * 장소 수정
     * - 로그인 여부 확인
     * - 작성자 본인만 수정 가능
     */
    public Place update(Long id,
                        String name,
                        String address,
                        String category,
                        Integer rating,
                        String imageUrl,
                        String memo,
                        User editor) {

        if (editor == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Place p = find(id);

        // 작성자 검증
        if (p.getAuthor() == null ||
                !p.getAuthor().getId().equals(editor.getId())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        // 수정 필드 반영
        p.setName(name);
        p.setAddress(address);
        p.setCategory(category);
        p.setRating(rating);
        p.setImageUrl(imageUrl);
        p.setMemo(memo);

        return repo.save(p);
    }

    /**
     * 장소 삭제
     * - 로그인 필수
     * - 작성자 본인만 삭제 가능
     */
    public void delete(Long id, User requester) {

        if (requester == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Place p = find(id);

        // 작성자 검증
        if (p.getAuthor() == null ||
                !p.getAuthor().getId().equals(requester.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }

        repo.delete(p);
    }

    /**
     * 장소 즐겨찾기 토글
     * - 이미 즐겨찾기한 경우: 삭제
     * - 없는 경우: 새로 생성
     */
    public boolean toggleFavorite(Long placeId, User user) {

        if (user == null) {
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        Place place = find(placeId);

        Optional<PlaceFavorite> existing =
                favRepo.findByUserIdAndPlaceId(user.getId(), placeId);

        if (existing.isPresent()) {
            favRepo.delete(existing.get());
            return false; // 즐겨찾기 해제
        } else {
            favRepo.save(
                    PlaceFavorite.builder()
                            .user(user)
                            .place(place)
                            .build()
            );
            return true; // 즐겨찾기 추가
        }
    }

    /**
     * 특정 사용자가 해당 장소를 즐겨찾기 했는지 여부
     */
    public boolean isFavorited(Long placeId, Long userId) {
        if (userId == null) return false;
        return favRepo.findByUserIdAndPlaceId(userId, placeId).isPresent();
    }

    /**
     * 특정 장소의 즐겨찾기 수
     */
    public long favoriteCount(Long placeId) {
        return favRepo.countByPlaceId(placeId);
    }

    /**
     * 장소 전체 목록 조회 (DTO 변환)
     * - 리뷰 작성 시 대상 선택용
     */
    public List<PlaceDTO> findAllPlaces() {
        List<Place> places =
                repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        return places.stream()
                .map(PlaceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 장소 검색 (비페이징)
     * - 자동완성 / 검색 API 용도
     */
    public List<Place> search(String keyword) {
        return repo.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                keyword,
                keyword
        );
    }
}
