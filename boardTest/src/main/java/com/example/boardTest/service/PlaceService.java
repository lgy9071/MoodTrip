package com.example.boardTest.service;

import com.example.boardTest.entity.Place;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.PlaceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceService {
    private final PlaceRepository repo;

    public Page<Place> list(String keyword, int page, int size) {
        page = Math.max(0, page);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (keyword == null || keyword.isBlank()) return repo.findAll(pageable);
        return repo.findByCategoryOrName(keyword, keyword, pageable);
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
        Place p = find(id);
        if (!p.getAuthor().getId().equals(editor.getId())) throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        p.setName(name); p.setAddress(address); p.setCategory(category);
        p.setRating(rating); p.setImageUrl(imageUrl); p.setMemo(memo);
        return repo.save(p);
    }

    public void delete(Long id, User requester) {
        Place p = find(id);
        if (!p.getAuthor().getId().equals(requester.getId())) throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        repo.delete(p);
    }
}
