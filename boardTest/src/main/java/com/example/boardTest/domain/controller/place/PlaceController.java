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
                       @RequestParam(required = false, defaultValue = "false") boolean favoritesOnly,
                       Model model,
                       HttpSession session) {

        User me = (User) session.getAttribute("LOGIN_USER");
        Long favUserId = (favoritesOnly && me != null) ? me.getId() : null;

        Page<Place> pg = service.list(keyword, page, 8, favUserId);
        int total = pg.getTotalPages();

        if (page > 0 && page >= total) {
            return "redirect:/places?page=" + Math.max(0, total - 1)
                    + (keyword != null ? "&keyword=" + keyword : "")
                    + (favoritesOnly ? "&favoritesOnly=true" : "");
        }

        model.addAttribute("page", pg.map(PlaceDTO::fromEntity));
        model.addAttribute("currentPage", pg.getNumber());
        model.addAttribute("totalPages", total);
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
                         HttpSession session) {

        User user = (User) session.getAttribute("LOGIN_USER");

        service.create(dto.getName(), dto.getAddress(), dto.getCategory(),
                dto.getRating(), dto.getImageUrl(), dto.getMemo(), user);

        return "redirect:/places";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, HttpSession session) {

        Place place = service.find(id);
        User me = (User) session.getAttribute("LOGIN_USER");

        boolean isOwner = (me != null && place.getAuthor() != null
                && me.getId().equals(place.getAuthor().getId()));
        boolean isFav = service.isFavorited(id, me != null ? me.getId() : null);
        long favCount = service.favoriteCount(id);

        model.addAttribute("place", PlaceDTO.fromEntity(place));
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("isFav", isFav);
        model.addAttribute("favCount", favCount);

        return "places/detail";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @ModelAttribute PlaceCreateRequest dto,
                       HttpSession session) {

        User user = (User) session.getAttribute("LOGIN_USER");

        service.update(id, dto.getName(), dto.getAddress(),
                dto.getCategory(), dto.getRating(),
                dto.getImageUrl(), dto.getMemo(), user);

        return "redirect:/places/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, HttpSession session) {

        User user = (User) session.getAttribute("LOGIN_USER");

        service.delete(id, user);

        return "redirect:/places";
    }

    @PostMapping("/{id}/favorite")
    public String toggleFavorite(@PathVariable Long id,
                                 @RequestParam(required = false) String back,
                                 HttpSession session,
                                 RedirectAttributes ra) {

        User me = (User) session.getAttribute("LOGIN_USER");

        boolean nowFav = service.toggleFavorite(id, me);
        ra.addFlashAttribute("msg", nowFav ? "즐겨찾기에 추가했어요." : "즐겨찾기를 해제했어요.");

        return (back != null && !back.isBlank()) ?
                "redirect:" + back :
                "redirect:/places/" + id;
    }
}
