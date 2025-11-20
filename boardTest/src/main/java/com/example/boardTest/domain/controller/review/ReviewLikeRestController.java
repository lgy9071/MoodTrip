package com.example.boardTest.domain.controller.review;

import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.review.ReviewLikeService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewLikeRestController {

    private final ReviewLikeService likeService;

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> toggleLike(
            @PathVariable(name = "id") Long id,
            HttpSession session) {

        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if (currentUser == null) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "로그인이 필요합니다."));
        }

        boolean liked = likeService.toggleLike(id, currentUser);
        long likeCount = likeService.countLikes(id);

        return ResponseEntity.ok(Map.of(
                "liked", liked,
                "likeCount", likeCount
        ));
    }
}