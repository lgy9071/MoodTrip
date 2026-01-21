package com.example.boardTest.domain.controller.review;

import com.example.boardTest.domain.service.place.PlaceService;
import com.example.boardTest.domain.service.post.PostService;
import com.example.boardTest.domain.service.trip.TripService;
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

    /**
     * 리뷰 대상 검색 API
     * - 영화 / 여행 / 장소
     */
    @GetMapping("/search-targets")
    public List<Map<String, Object>> searchTargets(
            @RequestParam String category,
            @RequestParam String keyword) {

        List<Map<String, Object>> results = new ArrayList<>();

        switch (category) {

            case "movie" ->
                    postService.search(keyword).forEach(post ->
                            results.add(Map.of(
                                    "id", post.getId(),
                                    "name", post.getTitle()
                            )));

            case "trip" ->
                    tripService.search(keyword).forEach(plan ->
                            results.add(Map.of(
                                    "id", plan.getId(),
                                    "name", plan.getTitle()
                            )));

            case "place" ->
                    placeService.search(keyword).forEach(place ->
                            results.add(Map.of(
                                    "id", place.getId(),
                                    "name", place.getName()
                            )));
        }

        return results;
    }
}
