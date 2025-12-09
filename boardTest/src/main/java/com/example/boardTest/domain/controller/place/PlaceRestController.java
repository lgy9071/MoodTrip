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

    @GetMapping
    public List<Place> list(@RequestParam(required = false) String keyword, HttpSession session) {
        User me = (User) session.getAttribute("LOGIN_USER");
        Long favUserId = (me != null) ? me.getId() : null;
        return service.list(keyword, 0, 50, favUserId).getContent();
    }

    @GetMapping("/{id}")
    public Place detail(@PathVariable Long id) {
        return service.find(id);
    }

    @PostMapping("/{id}/favorite")
    public boolean toggleFavorite(@PathVariable Long id, HttpSession session) {
        User me = (User) session.getAttribute("LOGIN_USER");
        return service.toggleFavorite(id, me);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, HttpSession session) {
        User me = (User) session.getAttribute("LOGIN_USER");
        service.delete(id, me);
    }
}