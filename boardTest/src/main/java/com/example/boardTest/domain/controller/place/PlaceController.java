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

    // 장소 관련 비즈니스 로직 담당 Service
    private final PlaceService service;

    /**
     * 장소 목록 페이지
     * - 검색(keyword)
     * - 즐겨찾기만 보기(favoritesOnly)
     * - 페이징
     * - @SessionAttribute로 로그인 사용자 강제 주입
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "false") boolean favoritesOnly,
                       @SessionAttribute(name = "LOGIN_USER") User me,
                       Model model) {

        // 즐겨찾기 필터가 켜져 있으면 로그인 사용자 ID 사용
        Long favUserId = (favoritesOnly ? me.getId() : null);

        // 장소 목록 조회 (Page 객체)
        Page<Place> pg = service.list(keyword, page, 8, favUserId);

        // View 전달 데이터
        model.addAttribute("page", pg.map(PlaceDTO::fromEntity));
        model.addAttribute("currentPage", pg.getNumber());
        model.addAttribute("totalPages", pg.getTotalPages());
        model.addAttribute("keyword", keyword);
        model.addAttribute("favoritesOnly", favoritesOnly);

        return "places/list";
    }

    /**
     * 장소 등록 폼
     * - 로그인 여부는 Interceptor에서 처리된다는 전제
     */
    @GetMapping("/new")
    public String form() {
        return "places/form";
    }

    /**
     * 장소 등록 처리
     */
    @PostMapping
    public String create(@ModelAttribute(name = "dto") PlaceCreateRequest dto,
                         @SessionAttribute(name = "LOGIN_USER") User me) {

        service.create(
                dto.getName(),
                dto.getAddress(),
                dto.getCategory(),
                dto.getRating(),
                dto.getImageUrl(),
                dto.getMemo(),
                me
        );

        return "redirect:/places";
    }

    /**
     * 장소 상세 페이지
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id,
                         @SessionAttribute(name = "LOGIN_USER") User me,
                         Model model) {

        // 장소 조회
        Place place = service.find(id);

        // View 데이터 구성
        model.addAttribute("place", PlaceDTO.fromEntity(place));

        // 작성자 여부
        model.addAttribute("isOwner",
                me.getId().equals(place.getAuthor().getId()));

        // 즐겨찾기 여부
        model.addAttribute("isFav",
                service.isFavorited(id, me.getId()));

        // 즐겨찾기 수
        model.addAttribute("favCount",
                service.favoriteCount(id));

        return "places/detail";
    }
}
