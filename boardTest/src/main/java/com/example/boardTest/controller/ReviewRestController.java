package com.example.boardTest.controller;

import com.example.boardTest.service.PlaceService;
import com.example.boardTest.service.PostService;
import com.example.boardTest.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewRestController {

    private final PostService postService;
    private final TripService tripService;
    private final PlaceService placeService;

    @GetMapping("/search-targets")
    public List<Map<String, Object>> searchTargets(
            @RequestParam String category,
            @RequestParam String keyword) {

        List<Map<String, Object>> results = new ArrayList<>();

        switch (category) {
            case "movie" -> postService.search(keyword).forEach(post -> {
                results.add(Map.of(
                        "id", post.getId(),
                        "name", post.getTitle()
                ));
            });

            case "trip" -> tripService.search(keyword).forEach(plan -> {
                results.add(Map.of(
                        "id", plan.getId(),
                        "name", plan.getTitle()
                ));
            });

            case "place" -> placeService.search(keyword).forEach(place -> {
                results.add(Map.of(
                        "id", place.getId(),
                        "name", place.getName()
                ));
            });
        }
        return results;
    }
}
