package com.example.boardTest.domain.controller.trip;

import com.example.boardTest.domain.dto.trip.StopSuggestRequest;
import com.example.boardTest.domain.dto.trip.SuggestedStopDTO;
import com.example.boardTest.domain.service.trip.TripAiSuggestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ai/trips")
@RequiredArgsConstructor
public class TripAiController {

    private final TripAiSuggestService suggestService;

    /**
     * AI 기반 경유지 추천
     * - 일정 기반 추천
     * - 로그인 비필수
     */
    @PostMapping("/suggest-stops")
    public List<SuggestedStopDTO> suggestStops(
            @Valid @RequestBody StopSuggestRequest req) {

        return suggestService.suggest(req);
    }
}
