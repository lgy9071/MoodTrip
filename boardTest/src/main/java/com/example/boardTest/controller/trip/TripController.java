package com.example.boardTest.controller.trip;

import com.example.boardTest.domain.trip.TripCostCategory;
import com.example.boardTest.dto.trip.NewStopDTO;
import com.example.boardTest.dto.trip.TripPlanCreateDTO;
import com.example.boardTest.dto.trip.TripStopCreateDTO;
import com.example.boardTest.entity.trip.TripPlan;
import com.example.boardTest.entity.trip.TripStop;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.trip.TripService;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.net.URLEncoder;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/trips")
@RequiredArgsConstructor
public class TripController {

    private final TripService tripService;

    // 세션 키 이름(프로젝트에서 사용하는 키)
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
                          RedirectAttributes ra,
                          @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {

        if (loginUser == null) {
            String next = URLEncoder.encode("/trips/new", StandardCharsets.UTF_8);
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login?next=" + next;
        }

        TripPlanCreateDTO form = new TripPlanCreateDTO();
        form.setTitle("");
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(2));
        if (form.getStops() == null) form.setStops(new ArrayList<>());
        // 초기 1행(빈)
        form.getStops().add(new NewStopDTO(1, "", null, null, BigDecimal.ZERO, TripCostCategory.OTHER, null));

        model.addAttribute("form", form);
        model.addAttribute("categories", TripCostCategory.values());
        return "trips/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TripPlanCreateDTO form,
                         BindingResult br,
                         RedirectAttributes ra,
                         Model model,
                         @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {

        if (loginUser == null) {
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        if (!form.isDateRangeValid()) {
            br.rejectValue("endDate", "dateRange", "종료일은 시작일 이후여야 합니다.");
        }
        if (br.hasErrors()) {
            model.addAttribute("categories", TripCostCategory.values());
            return "trips/new";
        }

        TripPlan plan = tripService.createPlan(
                form.getTitle(), form.getStartDate(), form.getEndDate(), loginUser);

        // 여러 경유지 저장 (이미지 포함)
        if (form.getStops() != null) {
            for (NewStopDTO s : form.getStops()) {
                if (s.getPlaceName() == null || s.getPlaceName().isBlank()) continue;

                String imageUrl = null;
                MultipartFile image = s.getImage();
                if (image != null && !image.isEmpty()) {
                    imageUrl = tripService.saveTripImage(plan.getId(), image);
                }

                TripStopCreateDTO dto = new TripStopCreateDTO(
                        s.getDayOrder(),
                        s.getPlaceName(),
                        s.getAddress(),
                        s.getMemo(),
                        s.getCost() != null ? s.getCost() : BigDecimal.ZERO,
                        s.getCategory() != null ? s.getCategory() : TripCostCategory.OTHER,
                        imageUrl,
                        null
                );
                tripService.addStop(plan.getId(), dto, loginUser);
            }
        }

        ra.addFlashAttribute("msg", "여행 계획을 등록했습니다.");
        return "redirect:/trips";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable(name = "id") Long id,
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
        model.addAttribute("stopsByDay", stopsByDay);

        // 차트용 값은 JS에서 NaN 방지 위해 Double로
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
            catValues.add(v == null ? 0d : v.doubleValue());
        }

        model.addAttribute("plan", plan);
        model.addAttribute("stops", stops);
        model.addAttribute("stopForm",
                           new TripStopCreateDTO(1, "", "", "", BigDecimal.ZERO, TripCostCategory.OTHER, null, null));
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("dayCost", dayCost);
        model.addAttribute("isOwner", isOwner);

        model.addAttribute("categories", TripCostCategory.values());
        model.addAttribute("catLabels", catLabels);
        model.addAttribute("catValues", catValues);

        return "trips/detail";
    }

    @PostMapping("/{id}/stops")
    public String addStop(@PathVariable("id") Long id,
                          @Valid @ModelAttribute("stopForm") TripStopCreateDTO dto,
                          BindingResult br,
                          RedirectAttributes ra,
                          Model model,
                          @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {

        if (loginUser == null) {
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        if (br.hasErrors()) {
            TripPlan plan = tripService.findPlan(id);
            model.addAttribute("plan", plan);
            model.addAttribute("stops", tripService.getStops(id));
            model.addAttribute("categories", TripCostCategory.values());
            return "trips/detail";
        }

        // 이미지 저장
        String imageUrl = null;
        if (dto.image() != null && !dto.image().isEmpty()) {
            imageUrl = tripService.saveTripImage(id, dto.image());
        }

        // imageUrl 주입해서 다시 DTO 구성
        TripStopCreateDTO withUrl = new TripStopCreateDTO(
                dto.dayOrder(),
                dto.placeName(),
                dto.address(),
                dto.memo(),
                dto.cost(),
                dto.category(),
                dto.imageUrl(),
                null
        );

        tripService.addStop(id, withUrl, loginUser);
        ra.addFlashAttribute("msg", "경유지를 추가했습니다.");
        return "redirect:/trips/{id}";
    }

    @PostMapping("/{id}/delete")
    public String deletePlan(@PathVariable("id") Long id,
                             RedirectAttributes ra,
                             @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {

        if (loginUser == null) {
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        tripService.deletePlan(id, loginUser);
        ra.addFlashAttribute("msg", "여행 계획을 삭제했습니다.");
        return "redirect:/trips";
    }
}
