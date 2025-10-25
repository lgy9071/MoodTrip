package com.example.boardTest.controller;

import com.example.boardTest.domain.trip.TripCostCategory;
import com.example.boardTest.dto.trip.NewStopDTO;
import com.example.boardTest.dto.trip.TripPlanCreateDTO;
import com.example.boardTest.dto.trip.TripStopCreateDTO;
import com.example.boardTest.entity.TripPlan;
import com.example.boardTest.entity.TripStop;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.TripService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.net.URLEncoder;

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
            // 로그인 후 다시 돌아올 위치를 next에 담아 전달
            String next = URLEncoder.encode("/trips/new", StandardCharsets.UTF_8);
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login?next=" + next;
        }

        TripPlanCreateDTO form = new TripPlanCreateDTO(); // 기본 생성자
        form.setTitle("");
        form.setStartDate(LocalDate.now());
        form.setEndDate(LocalDate.now().plusDays(2));

        // stops 기본 1행(빈 행) 제공
        if (form.getStops() == null) form.setStops(new ArrayList<>());
        form.getStops().add(new NewStopDTO(1, "", null, null, BigDecimal.ZERO, TripCostCategory.OTHER));

        model.addAttribute("form", form);
        model.addAttribute("categories", TripCostCategory.values());
        return "trips/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TripPlanCreateDTO form,
                         BindingResult br,
                         RedirectAttributes ra,
                         Model model,   // 뷰로 돌려보낼 때 categories 재주입
                         @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {

        if (loginUser == null) {
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        if (!form.isDateRangeValid()) {
            br.rejectValue("endDate", "dateRange", "종료일은 시작일 이후여야 합니다.");
        }
        if (br.hasErrors()) {
            model.addAttribute("categories", TripCostCategory.values()); // 다시 넣어주기
            return "trips/new";
        }

        TripPlan plan = tripService.createPlan(
                form.getTitle(), form.getStartDate(), form.getEndDate(), loginUser);

        // 여러 경유지 저장 (getter 사용)
        if (form.getStops() != null) {
            for (NewStopDTO s : form.getStops()) {
                // 빈 행(장소명 비어있음)은 스킵
                if (s.getPlaceName() == null || s.getPlaceName().isBlank()) continue;

                TripStopCreateDTO dto = new TripStopCreateDTO(
                        s.getDayOrder(),
                        s.getPlaceName(),
                        s.getAddress(),
                        s.getMemo(),
                        s.getCost() != null ? s.getCost() : BigDecimal.ZERO,
                        s.getCategory() != null ? s.getCategory() : TripCostCategory.OTHER
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

        // 차트용 라벨/값 배열 (값을 Double로 보장하여 JS NaN 문제 방지)
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
                new TripStopCreateDTO(1, "", "", "", BigDecimal.ZERO, TripCostCategory.OTHER));
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("dayCost", dayCost);
        model.addAttribute("isOwner", isOwner);

        // 카테고리 셀렉트 옵션 & 차트 데이터
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
            return "trips/detail";
        }

        tripService.addStop(id, dto, loginUser);
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

    @DeleteMapping("/stops/{stopId}")
    @ResponseBody
    public void deleteStop(@PathVariable("stopId") Long stopId,
                           @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {
        // 필요시 서비스에서 소유자 검증
        tripService.removeStop(stopId);
    }

    @PatchMapping("/stops/{stopId}/day")
    @ResponseBody
    public void moveDay(@PathVariable("stopId") Long stopId,
                        @RequestParam("newDay") int newDay,
                        @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {
        tripService.moveDay(stopId, newDay);
    }

    // (선택) 상세 화면의 "수정" 모달을 실제 저장하려면 PUT 엔드포인트도 구현 필요
    // @PutMapping("/stops/{stopId}") ...
}
