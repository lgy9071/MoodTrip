package com.example.boardTest.domain.controller.trip;

import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.trip.TripService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
@RequiredArgsConstructor
public class TripRestController {

    private final TripService tripService;
    public static final String LOGIN_USER_ATTR = "LOGIN_USER";

    @DeleteMapping("/stops/{stopId}")
    public void deleteStop(@PathVariable(name = "stopId") Long stopId,
                           @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {
        tripService.removeStop(stopId);
    }

    @PatchMapping("/stops/{stopId}/day")
    public void moveDay(@PathVariable(name = "stopId") Long stopId,
                        @RequestParam(name = "newDay") int newDay,
                        @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {
        tripService.moveDay(stopId, newDay);
    }
}
