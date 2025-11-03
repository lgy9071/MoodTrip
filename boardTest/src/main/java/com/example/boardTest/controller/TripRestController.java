package com.example.boardTest.controller;

import com.example.boardTest.entity.User;
import com.example.boardTest.service.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripRestController {

    private final TripService tripService;
    public static final String LOGIN_USER_ATTR = "LOGIN_USER";

    // 경유지 삭제 API
    @DeleteMapping("/stops/{stopId}")
    public void deleteStop(@PathVariable("stopId") Long stopId,
                           @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {
        tripService.removeStop(stopId);
    }

    // 경유지 일자 변경 API
    @PatchMapping("/stops/{stopId}/day")
    public void moveDay(@PathVariable("stopId") Long stopId,
                        @RequestParam("newDay") int newDay,
                        @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {
        tripService.moveDay(stopId, newDay);
    }
}