package com.example.boardTest.controller;

import com.example.boardTest.entity.Place;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.PlaceService;
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
                       Model model) {
        Page<Place> pg = service.list(keyword, page, 8);
        int total = pg.getTotalPages();
        if (page > 0 && page >= total) return "redirect:/places?page=" + Math.max(0, total - 1) + (keyword != null ? "&keyword=" + keyword : "");
        model.addAttribute("page", pg);
        model.addAttribute("currentPage", pg.getNumber());
        model.addAttribute("totalPages", total);
        model.addAttribute("keyword", keyword);
        return "places/list";
    }

    @GetMapping("/new")
    public String form() {
        return "places/form";
    }

    @PostMapping
    public String create(@RequestParam("name") String name,
                         @RequestParam(required = false) String address,
                         @RequestParam(required = false) String category,
                         @RequestParam(required = false) Integer rating,
                         @RequestParam(required = false) String imageUrl,
                         @RequestParam(required = false) String memo,
                         HttpSession session,
                         RedirectAttributes ra) {
        User user = (User) session.getAttribute("LOGIN_USER");
        if (user == null) {
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login?next=/places/new";
        }
        service.create(name, address, category, rating, imageUrl, memo, user);
        return "redirect:/places";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model, HttpSession session) {
        Place place = service.find(id);
        User me = (User) session.getAttribute("LOGIN_USER");
        boolean isOwner = (me != null && place.getAuthor() != null
                && me.getId().equals(place.getAuthor().getId()));
        model.addAttribute("place", place);
        model.addAttribute("isOwner", isOwner);
        return "places/detail";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @RequestParam("name") String name,
                       @RequestParam(required = false) String address,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) Integer rating,
                       @RequestParam(required = false) String imageUrl,
                       @RequestParam(required = false) String memo,
                       HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        service.update(id, name, address, category, rating, imageUrl, memo, user);
        return "redirect:/places/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        service.delete(id, user);
        return "redirect:/places";
    }
}
