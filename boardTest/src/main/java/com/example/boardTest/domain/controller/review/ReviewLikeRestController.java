package com.example.boardTest.domain.controller.review;

import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.review.ReviewLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewLikeRestController {

    private final ReviewLikeService likeService;

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable Long id,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("LOGIN_USER");

        boolean liked = likeService.toggleLike(id, currentUser);
        long likeCount = likeService.countLikes(id);

        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
        ));
    }
}
