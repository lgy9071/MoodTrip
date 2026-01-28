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

    /**
     * 리뷰 좋아요 토글
     */
    @PostMapping("/{id}/like")
    public Map<String, Object> toggleLike(@PathVariable(name = "id") Long id,
                                          @SessionAttribute(name = "LOGIN_USER") User me) {

        boolean liked = likeService.toggleLike(id, me);

        return Map.of(
                "liked", liked,
                "likeCount", likeService.countLikes(id)
        );
    }
}
