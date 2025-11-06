package com.example.boardTest.service;

import com.example.boardTest.dto.place.PlaceDTO;
import com.example.boardTest.entity.place.Place;
import com.example.boardTest.entity.place.PlaceFavorite;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.place.PlaceFavoriteRepository;
import com.example.boardTest.repository.place.PlaceRepository;
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
    private final PlaceRepository repo;
    private final PlaceFavoriteRepository favRepo;

    public Page<Place> list(String keyword, int page, int size, Long favoritesOfUserId) {
        page = Math.max(0, page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (favoritesOfUserId != null) {
            // 즐겨찾기만
            return favRepo.findFavoritesByUserId(favoritesOfUserId, pageable);
        }
        if (keyword == null || keyword.isBlank()) return repo.findAll(pageable);
        return repo.findByCategoryContainingIgnoreCaseOrNameContainingIgnoreCase(keyword, keyword, pageable);
    }

    public Place find(Long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("장소를 찾을 수 없습니다."));
    }

    public Place create(String name, String address, String category, Integer rating, String imageUrl, String memo, User author) {
        Place p = Place.builder()
                .name(name).address(address).category(category)
                .rating(rating).imageUrl(imageUrl).memo(memo)
                .author(author).build();
        return repo.save(p);
    }

    public Place update(Long id, String name, String address, String category, Integer rating, String imageUrl, String memo, User editor) {
        if (editor == null) throw new IllegalStateException("로그인이 필요합니다.");
        Place p = find(id);
        if (p.getAuthor() == null || !p.getAuthor().getId().equals(editor.getId()))
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        p.setName(name); p.setAddress(address); p.setCategory(category);
        p.setRating(rating); p.setImageUrl(imageUrl); p.setMemo(memo);
        return repo.save(p);
    }

    public void delete(Long id, User requester) {
        if (requester == null) throw new IllegalStateException("로그인이 필요합니다.");
        Place p = find(id);
        if (p.getAuthor() == null || !p.getAuthor().getId().equals(requester.getId()))
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        repo.delete(p);
    }

    // 즐겨찾기 기능
    public boolean toggleFavorite(Long placeId, User user) {
        if (user == null) throw new IllegalStateException("로그인이 필요합니다.");
        Place place = find(placeId);
        Optional<PlaceFavorite> existing = favRepo.findByUserIdAndPlaceId(user.getId(), placeId);
        if (existing.isPresent()) {
            favRepo.delete(existing.get());
            return false; // un-favorited
        } else {
            favRepo.save(PlaceFavorite.builder().user(user).place(place).build());
            return true; // favorited
        }
    }

    public boolean isFavorited(Long placeId, Long userId) {
        if (userId == null) return false;
        return favRepo.findByUserIdAndPlaceId(userId, placeId).isPresent();
    }

    public long favoriteCount(Long placeId) {
        return favRepo.countByPlaceId(placeId);
    }

    public List<PlaceDTO> findAllPlaces() {
        List<Place> places = repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        return places.stream()
                .map(PlaceDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<Place> search(String keyword) {
        return repo.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCase(keyword, keyword);
    }
}
