package com.example.boardTest.domain.controller.place;

import com.example.boardTest.domain.entity.place.Place;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.place.PlaceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places")
public class PlaceRestController {

    private final PlaceService service;

    /**
     * 장소 목록 JSON 조회
     * - 지도, 자동완성, AJAX 용도
     * - HttpSession 사용 (API는 선택적 로그인)
     */
    @GetMapping
    public List<Place> list(@RequestParam(required = false) String keyword,
                            HttpSession session) {

        User me = (User) session.getAttribute("LOGIN_USER");
        Long favUserId = (me != null) ? me.getId() : null;

        return service.list(keyword, 0, 50, favUserId).getContent();
    }

    /**
     * 장소 단건 조회(JSON)
     */
    @GetMapping("/{id}")
    public Place detail(@PathVariable Long id) {
        return service.find(id);
    }

    /**
     * 즐겨찾기 토글 (AJAX)
     */
    @PostMapping("/{id}/favorite")
    public boolean toggleFavorite(@PathVariable Long id,
                                  HttpSession session) {

        User me = (User) session.getAttribute("LOGIN_USER");
        return service.toggleFavorite(id, me);
    }

    /**
     * 장소 삭제 API
     */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id,
                       HttpSession session) {

        User me = (User) session.getAttribute("LOGIN_USER");
        service.delete(id, me);
    }
}
