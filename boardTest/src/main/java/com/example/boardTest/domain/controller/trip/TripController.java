package com.example.boardTest.domain.controller.trip;

import com.example.boardTest.domain.entity.trip.TripCostCategory;
import com.example.boardTest.domain.dto.trip.NewStopDTO;
import com.example.boardTest.domain.dto.trip.TripPlanCreateDTO;
import com.example.boardTest.domain.dto.trip.TripStopCreateDTO;
import com.example.boardTest.domain.entity.trip.TripPlan;
import com.example.boardTest.domain.entity.trip.TripStop;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.trip.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 여행 계획(TripPlan) 관련 요청을 처리하는 Controller
 * - 목록 / 생성 / 상세 / 삭제
 * - 경유지(TripStop) 추가
 */
@Controller
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    /**
     * 세션에 저장된 로그인 사용자 키
     */
    public static final String LOGIN_USER_ATTR = "LOGIN_USER";

    /**
     * 여행 계획 목록 조회 (페이징)
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        // Page<TripPlan> 그대로 전달 → 뷰에서 페이징 정보 사용 가능
        Page<TripPlan> plans = tripService.listPlans(page, size);
        model.addAttribute("plans", plans);

        return "trips/list";
    }

    /**
     * 여행 계획 생성 폼
     */
    @GetMapping("/new")
    public String newForm(Model model,
                          @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        // 폼 바인딩 전용 DTO
        TripPlanCreateDTO form = new TripPlanCreateDTO();

        // UX를 위한 기본값
        form.setTitle("");
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(2));

        // stops 리스트 null 방지 (Thymeleaf 에러 방지)
        form.initIfEmpty();

        model.addAttribute("form", form);

        // 비용 카테고리 enum 전달
        model.addAttribute("categories", TripCostCategory.values());

        return "trips/new";
    }

    /**
     * 여행 계획 생성 처리
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("form") TripPlanCreateDTO form,
                         @RequestParam("thumbnail") MultipartFile multipartFile,
                         @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser,
                         Model model) {

        // 1️. 여행 계획 먼저 저장 (ID 확보 목적)
        TripPlan plan = tripService.createPlan(
                form.getTitle(),
                form.getStartDate(),
                form.getEndDate(),
                loginUser
        );

        // 2️. 썸네일 이미지 저장 (선택)
        if (multipartFile != null && !multipartFile.isEmpty()) {
            plan.setThumbnailUrl(
                    tripService.saveTripImage(plan.getId(), multipartFile)
            );
        }

        // 3️. 경유지(TripStop) 저장
        if (form.getStops() != null) {
            for (NewStopDTO stop : form.getStops()) {

                // 장소명이 없는 row는 무시
                if (stop.getPlaceName() == null || stop.getPlaceName().isBlank()) {
                    continue;
                }

                // 경유지 이미지 업로드
                String imageUrl = null;
                if (stop.getImage() != null && !stop.getImage().isEmpty()) {
                    imageUrl = tripService.saveTripImage(plan.getId(), stop.getImage());
                }

                // null-safe 처리
                TripStopCreateDTO dto = new TripStopCreateDTO(
                        stop.getDayOrder(),
                        stop.getPlaceName(),
                        stop.getAddress(),
                        stop.getMemo(),
                        Optional.ofNullable(stop.getCost()).orElse(BigDecimal.ZERO),
                        Optional.ofNullable(stop.getCategory()).orElse(TripCostCategory.OTHER),
                        imageUrl,
                        null
                );

                tripService.addStop(plan.getId(), dto, loginUser);
            }
        }

        return "redirect:/trips";
    }

    /**
     * 여행 계획 상세 조회
     */
    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id,
                         Model model,
                         @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        TripPlan plan = tripService.findPlan(id);
        List<TripStop> stops = tripService.getStops(id);

        model.addAttribute("plan", plan);
        model.addAttribute("stops", stops);

        // 일차(dayOrder) 기준으로 경유지 그룹핑
        Map<Integer, List<TripStop>> stopsByDay =
                stops.stream()
                        .collect(Collectors.groupingBy(
                                TripStop::getDayOrder,
                                TreeMap::new, // day 순서 보장
                                Collectors.toList()
                        ));

        model.addAttribute("stopsByDay", stopsByDay);

        // 비용 관련 데이터
        model.addAttribute("totalCost", tripService.totalCost(id));
        model.addAttribute("dayCost", tripService.dayCostMap(id));

        // 카테고리별 비용 차트용 데이터
        Map<TripCostCategory, BigDecimal> catCost =
                tripService.categoryCostMap(id);

        List<String> catLabels = new ArrayList<>();
        List<Double> catValues = new ArrayList<>();

        for (TripCostCategory c : TripCostCategory.values()) {
            catLabels.add(switch (c) {
                case TRANSPORT -> "교통";
                case LODGING -> "숙박";
                case FOOD -> "식사";
                case SIGHTSEEING -> "관광";
                case SHOPPING -> "쇼핑";
                default -> "기타";
            });

            BigDecimal v = catCost.getOrDefault(c, BigDecimal.ZERO);
            catValues.add(v.doubleValue());
        }

        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catValues", catValues);
        model.addAttribute("categories", TripCostCategory.values());

        // 작성자 여부
        model.addAttribute("isOwner",
                loginUser.getId().equals(plan.getOwner().getId()));

        // 경유지 추가 폼 기본값
        model.addAttribute("stopForm",
                new TripStopCreateDTO(
                        1,
                        "",
                        "",
                        "",
                        BigDecimal.ZERO,
                        TripCostCategory.OTHER,
                        null,
                        null
                ));

        return "trips/detail";
    }

    /**
     * 경유지 추가
     */
    @PostMapping("/{id}/stops")
    public String addStop(@PathVariable(name = "id") Long id,
                          @Valid @ModelAttribute("stopForm") TripStopCreateDTO dto,
                          @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        String imageUrl = null;
        if (dto.image() != null && !dto.image().isEmpty()) {
            imageUrl = tripService.saveTripImage(id, dto.image());
        }

        TripStopCreateDTO withUrl = new TripStopCreateDTO(
                dto.dayOrder(),
                dto.placeName(),
                dto.address(),
                dto.memo(),
                dto.cost(),
                dto.category(),
                imageUrl,
                null
        );

        tripService.addStop(id, withUrl, loginUser);
        return "redirect:/trips/" + id;
    }

    /**
     * 여행 계획 삭제 (작성자만 가능)
     */
    @PostMapping("/{id}/delete")
    public String deletePlan(@PathVariable(name = "id") Long id,
                             @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        tripService.deletePlan(id, loginUser);
        return "redirect:/trips";
    }
}
