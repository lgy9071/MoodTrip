package com.example.boardTest.domain.controller.place;

import com.example.boardTest.domain.dto.place.PlaceCreateRequest;
import com.example.boardTest.domain.dto.place.PlaceDTO;
import com.example.boardTest.domain.entity.place.Place;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.place.PlaceService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/places")
public class PlaceController {

    private final PlaceService service;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "false") boolean favoritesOnly,
                       @SessionAttribute(name = "LOGIN_USER") User me,
                       Model model) {

        Long favUserId = (favoritesOnly ? me.getId() : null);

        Page<Place> pg = service.list(keyword, page, 8, favUserId);
        model.addAttribute("page", pg.map(PlaceDTO::fromEntity));
        model.addAttribute("currentPage", pg.getNumber());
        model.addAttribute("totalPages", pg.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("favoritesOnly", favoritesOnly);
        return "places/list";
    }

    @GetMapping("/new")
    public String form() {
        return "places/form";
    }

    @PostMapping
    public String create(@ModelAttribute PlaceCreateRequest dto,
                         @SessionAttribute(name = "LOGIN_USER") User me) {

        service.create(dto.getName(), dto.getAddress(), dto.getCategory(),
                dto.getRating(), dto.getImageUrl(), dto.getMemo(), me);

        return "redirect:/places";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         @SessionAttribute(name = "LOGIN_USER") User me,
                         Model model) {

        Place place = service.find(id);

        model.addAttribute("place", PlaceDTO.fromEntity(place));
        model.addAttribute("isOwner", me.getId().equals(place.getAuthor().getId()));
        model.addAttribute("isFav", service.isFavorited(id, me.getId()));
        model.addAttribute("favCount", service.favoriteCount(id));

        return "places/detail";
    }
}
