package com.example.boardTest.controller;

import com.example.boardTest.dto.review.ReviewCreateRequest;
import com.example.boardTest.dto.review.ReviewListDTO;
import com.example.boardTest.dto.review.ReviewUpdateRequest;
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
import org.springframework.web.multipart.MultipartFile;

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
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "5") int size,
                       @RequestParam(defaultValue = "latest") String sort,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false, defaultValue = "false") boolean mine,
                       HttpSession session,
                       Model model) {

        User currentUser = (User) session.getAttribute("LOGIN_USER");

        Page<ReviewListDTO> reviewPage = reviewService.getFilteredReviews(
                page, size, sort, category, mine ? currentUser : null);

        model.addAttribute("reviewPage", reviewPage);
        model.addAttribute("currentPage", reviewPage.getNumber());
        model.addAttribute("totalPages", reviewPage.getTotalPages());
        model.addAttribute("sort", sort);
        model.addAttribute("category", category);
        model.addAttribute("mine", mine);

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
    public String create(@ModelAttribute ReviewCreateRequest dto, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        if (currentUser == null)
            throw new IllegalStateException("로그인이 필요합니다.");

        reviewService.saveReview(
                dto.getTitle(),
                dto.getContent(),
                dto.getRating(),
                currentUser,
                dto.getCategory(),
                dto.getTargetId(),
                dto.getImage()
        );
        return "redirect:/reviews";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        Review review = reviewService.getReview(id);
        model.addAttribute("review", review);
        return "reviews/detail";
    }

    // 삭제
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        reviewService.deleteReview(id, currentUser);
        return "redirect:/reviews";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, HttpSession session) {
        User currentUser = (User) session.getAttribute("LOGIN_USER");
        Review review = reviewService.getReview(id);

        if (!review.getAuthor().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("본인만 수정할 수 있습니다.");
        }

        model.addAttribute("review", review);
        return "reviews/edit";
    }

    // 수정 처리
    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @ModelAttribute ReviewUpdateRequest dto,
                         HttpSession session) {

        User currentUser = (User) session.getAttribute("LOGIN_USER");
        reviewService.updateReview(
                id,
                dto.getTitle(),
                dto.getContent(),
                dto.getRating(),
                dto.getImage(),
                currentUser
        );
        return "redirect:/reviews/" + id;
    }
}