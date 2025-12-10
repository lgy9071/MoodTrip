package com.example.boardTest.domain.controller.review;

import com.example.boardTest.domain.dto.review.ReviewCreateRequest;
import com.example.boardTest.domain.dto.review.ReviewListDTO;
import com.example.boardTest.domain.dto.review.ReviewUpdateRequest;
import com.example.boardTest.domain.entity.review.Review;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.place.PlaceService;
import com.example.boardTest.domain.service.post.PostService;
import com.example.boardTest.domain.service.review.ReviewService;
import com.example.boardTest.domain.service.trip.TripService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
                       @RequestParam(defaultValue = "false") boolean mine,
                       @SessionAttribute(name = "LOGIN_USER") User me,
                       Model model) {

        Page<ReviewListDTO> pg = reviewService.getFilteredReviews(
                page, size, sort, category, mine ? me : null);

        model.addAttribute("reviewPage", pg);
        model.addAttribute("currentPage", pg.getNumber());
        model.addAttribute("totalPages", pg.getTotalPages());
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
    public String create(@Valid @ModelAttribute ReviewCreateRequest dto,
                         @SessionAttribute(name = "LOGIN_USER") User me) {

        reviewService.saveReview(
                dto.getTitle(),
                dto.getContent(),
                dto.getRating(),
                me,
                dto.getCategory(),
                dto.getTargetId(),
                dto.getImage()
        );

        return "redirect:/reviews";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("review", reviewService.getReview(id));
        return "reviews/detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @SessionAttribute(name = "LOGIN_USER") User me) {

        reviewService.deleteReview(id, me);
        return "redirect:/reviews";
    }
}

