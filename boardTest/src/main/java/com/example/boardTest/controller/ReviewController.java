package com.example.boardTest.controller;

import com.example.boardTest.entity.board.Post;
import com.example.boardTest.entity.Review;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.PlaceService;
import com.example.boardTest.service.PostService;
import com.example.boardTest.service.ReviewService;
import com.example.boardTest.service.TripService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final PostService postService;
    private final TripService tripService;
    private final PlaceService placeService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        if (page < 0) page = 0;
        Page<Review> reviewPage = reviewService.getReviews(page, 5);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("currentPage", reviewPage.getNumber());
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        return "reviews/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("movies", postService.findAll());
        model.addAttribute("trips", tripService.findAllPlans());
        model.addAttribute("places", placeService.findAllPlaces());
        return "reviews/new";
    }

    @PostMapping
    public String create(@RequestParam("title") String title,
                         @RequestParam("content") String content,
                         @RequestParam("rating") int rating,
                         @RequestParam("category") String category,
                         @RequestParam("targetId") Long targetId,
                         HttpSession session) {

        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if(currentUser == null){
            throw new IllegalStateException("로그인이 필요합니다.");
        }

        reviewService.saveReview(title, content, rating, currentUser, category, targetId);
        return "redirect:/reviews";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Review review = reviewService.getReview(id);
        model.addAttribute("review", review);
        return "reviews/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable(name = "id") Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        reviewService.deleteReview(id, currentUser);
        return "redirect:/reviews";
    }
}