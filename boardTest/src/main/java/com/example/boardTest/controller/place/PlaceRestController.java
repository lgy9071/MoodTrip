package com.example.boardTest.controller.place;

import com.example.boardTest.entity.place.Place;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.PlaceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceRestController {

    private final PlaceService service;

    // 전체 목록 JSON 조회
    @GetMapping
    public List<Place> list(@RequestParam(required = false) String keyword,
                            HttpSession session) {
        User me = (User) session.getAttribute("LOGIN_USER");
        Long favUserId = (me != null) ? me.getId() : null;
        // 단순 리스트만 반환
        return service.list(keyword, 0, 50, favUserId).getContent();
    }

    // 단일 장소 상세 조회 (JSON)
    @GetMapping("/{id}")
    public Place detail(@PathVariable("id") Long id) {
        return service.find(id);
    }

    // 즐겨찾기 토글 (AJAX 요청용)
    @PostMapping("/{id}/favorite")
    public boolean toggleFavorite(@PathVariable("id") Long id, HttpSession session) {
        User me = (User) session.getAttribute("LOGIN_USER");
        if (me == null) throw new RuntimeException("로그인이 필요합니다.");
        return service.toggleFavorite(id, me);
    }

    // 장소 삭제 (API)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id, HttpSession session) {
        User me = (User) session.getAttribute("LOGIN_USER");
        service.delete(id, me);
    }
}