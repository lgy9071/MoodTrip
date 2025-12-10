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
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;
    public static final String LOGIN_USER_ATTR = "LOGIN_USER";

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {

        Page<TripPlan> plans = tripService.listPlans(page, size);
        model.addAttribute("plans", plans);
        return "trips/list";
    }

    @GetMapping("/new")
    public String newForm(Model model,
                          @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        TripPlanCreateDTO form = new TripPlanCreateDTO();
        form.setTitle("");
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(2));
        form.initIfEmpty();

        model.addAttribute("form", form);
        model.addAttribute("categories", TripCostCategory.values());
        return "trips/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TripPlanCreateDTO form,
                         @RequestParam("thumbnail") MultipartFile multipartFile,
                         @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser,
                         Model model) {

        TripPlan plan = tripService.createPlan(
                form.getTitle(), form.getStartDate(), form.getEndDate(), loginUser
        );

        if (multipartFile != null && !multipartFile.isEmpty()) {
            plan.setThumbnailUrl(tripService.saveTripImage(plan.getId(), multipartFile));
        }

        // 경유지 저장
        if (form.getStops() != null) {
            for (NewStopDTO stop : form.getStops()) {

                if (stop.getPlaceName() == null || stop.getPlaceName().isBlank()) continue;

                String imageUrl = null;
                if (stop.getImage() != null && !stop.getImage().isEmpty()) {
                    imageUrl = tripService.saveTripImage(plan.getId(), stop.getImage());
                }

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

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        TripPlan plan = tripService.findPlan(id);
        List<TripStop> stops = tripService.getStops(id);

        model.addAttribute("plan", plan);
        model.addAttribute("stops", stops);

        Map<Integer, List<TripStop>> stopsByDay =
                stops.stream().collect(Collectors.groupingBy(
                        TripStop::getDayOrder, TreeMap::new, Collectors.toList()
                ));

        model.addAttribute("stopsByDay", stopsByDay);
        model.addAttribute("totalCost", tripService.totalCost(id));
        model.addAttribute("dayCost", tripService.dayCostMap(id));

        // 카테고리별 비용
        Map<TripCostCategory, BigDecimal> catCost = tripService.categoryCostMap(id);
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

        model.addAttribute("categories", TripCostCategory.values());
        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catValues", catValues);

        model.addAttribute("isOwner",
                loginUser.getId().equals(plan.getOwner().getId()));

        model.addAttribute("stopForm",
                new TripStopCreateDTO(1, "", "", "", BigDecimal.ZERO, TripCostCategory.OTHER, null, null));

        return "trips/detail";
    }

    @PostMapping("/{id}/stops")
    public String addStop(@PathVariable Long id,
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

    @PostMapping("/{id}/delete")
    public String deletePlan(@PathVariable Long id,
                             @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        tripService.deletePlan(id, loginUser);
        return "redirect:/trips";
    }
}
