package com.example.boardTest.controller;

import com.example.boardTest.entity.Post;
import com.example.boardTest.entity.Review;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.PostService;
import com.example.boardTest.service.ReviewService;
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
        List<Post> movies = postService.findAll();
        model.addAttribute("movies", movies);
        return "reviews/new";
    }

    @PostMapping
    public String create(@RequestParam("title") String title,
                         @RequestParam("content") String content,
                         @RequestParam("rating") int rating,
                         HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        reviewService.saveReview(title, content, rating, currentUser);
        return "redirect:/reviews";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Review review = reviewService.getReview(id);
        model.addAttribute("review", review);
        return "reviews/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        reviewService.deleteReview(id, currentUser);
        return "redirect:/reviews";
    }
}