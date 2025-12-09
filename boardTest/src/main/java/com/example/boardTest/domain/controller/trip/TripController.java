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
    public String newForm(Model model) {

        TripPlanCreateDTO form = new TripPlanCreateDTO();
        form.setTitle("");
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(2));
        form.setStops(new ArrayList<>());
        form.getStops().add(new NewStopDTO(1, "", null, null, BigDecimal.ZERO, TripCostCategory.OTHER, null));

        model.addAttribute("form", form);
        model.addAttribute("categories", TripCostCategory.values());

        return "trips/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TripPlanCreateDTO form,
                         @RequestParam("thumbnail") MultipartFile multipartFile,
                         BindingResult br,
                         RedirectAttributes ra,
                         @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser,
                         Model model) {

        if (!form.isDateRangeValid()) {
            br.rejectValue("endDate", "dateRange", "종료일은 시작일 이후여야 합니다.");
        }
        if (br.hasErrors()) {
            model.addAttribute("categories", TripCostCategory.values());
            return "trips/new";
        }

        TripPlan plan = tripService.createPlan(
                form.getTitle(), form.getStartDate(), form.getEndDate(), loginUser);

        if (multipartFile != null && !multipartFile.isEmpty()) {
            String thumbUrl = tripService.saveTripImage(plan.getId(), multipartFile);
            plan.setThumbnailUrl(thumbUrl);
        }

        if (form.getStops() != null) {
            for (NewStopDTO s : form.getStops()) {
                if (s.getPlaceName() == null || s.getPlaceName().isBlank()) continue;

                String imageUrl = null;
                if (s.getImage() != null && !s.getImage().isEmpty()) {
                    imageUrl = tripService.saveTripImage(plan.getId(), s.getImage());
                }

                TripStopCreateDTO dto = new TripStopCreateDTO(
                        s.getDayOrder(),
                        s.getPlaceName(),
                        s.getAddress(),
                        s.getMemo(),
                        s.getCost() != null ? s.getCost() : BigDecimal.ZERO,
                        s.getCategory() != null ? s.getCategory() : TripCostCategory.OTHER,
                        imageUrl,
                        s.getImage()
                );

                tripService.addStop(plan.getId(), dto, loginUser);
            }
        }

        ra.addFlashAttribute("msg", "여행 계획을 등록했습니다.");
        return "redirect:/trips";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id,
                         Model model,
                         @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {

        TripPlan plan = tripService.findPlan(id);
        List<TripStop> stops = tripService.getStops(id);

        BigDecimal totalCost = tripService.totalCost(id);
        Map<Integer, BigDecimal> dayCost = tripService.dayCostMap(id);
        Map<TripCostCategory, BigDecimal> catCost = tripService.categoryCostMap(id);

        boolean isOwner = (loginUser != null && plan.getOwner() != null &&
                loginUser.getId().equals(plan.getOwner().getId()));

        Map<Integer, List<TripStop>> stopsByDay = stops.stream()
                .collect(Collectors.groupingBy(TripStop::getDayOrder, TreeMap::new, Collectors.toList()));

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
            catValues.add(catCost.getOrDefault(c, BigDecimal.ZERO).doubleValue());
        }

        model.addAttribute("plan", plan);
        model.addAttribute("stops", stops);
        model.addAttribute("stopsByDay", stopsByDay);
        model.addAttribute("stopForm", new TripStopCreateDTO(1, "", "", "", BigDecimal.ZERO, TripCostCategory.OTHER, null, null));
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("dayCost", dayCost);
        model.addAttribute("isOwner", isOwner);
        model.addAttribute("categories", TripCostCategory.values());
        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catValues", catValues);

        return "trips/detail";
    }

    @PostMapping("/{id}/stops")
    public String addStop(@PathVariable Long id,
                          @Valid @ModelAttribute("stopForm") TripStopCreateDTO dto,
                          BindingResult br,
                          RedirectAttributes ra,
                          Model model,
                          @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        if (br.hasErrors()) {
            TripPlan plan = tripService.findPlan(id);
            model.addAttribute("plan", plan);
            model.addAttribute("stops", tripService.getStops(id));
            model.addAttribute("categories", TripCostCategory.values());
            return "trips/detail";
        }

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
        ra.addFlashAttribute("msg", "경유지를 추가했습니다.");
        return "redirect:/trips/{id}";
    }

    @PostMapping("/{id}/delete")
    public String deletePlan(@PathVariable Long id,
                             RedirectAttributes ra,
                             @SessionAttribute(name = LOGIN_USER_ATTR) User loginUser) {

        tripService.deletePlan(id, loginUser);
        ra.addFlashAttribute("msg", "여행 계획을 삭제했습니다.");
        return "redirect:/trips";
    }
}
