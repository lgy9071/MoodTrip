package com.example.boardTest.controller;

import com.example.boardTest.dto.TripPlanCreateDTO;
import com.example.boardTest.dto.TripStopCreateDTO;
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
import org.springframework.web.util.UriUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
            String next = UriUtils.encode("/trips/new", StandardCharsets.UTF_8);
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login?next=" + next;
        }

        model.addAttribute("form",
                           new TripPlanCreateDTO("", LocalDate.now(), LocalDate.now().plusDays(2)));
        return "trips/new";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") TripPlanCreateDTO form,
                         BindingResult br,
                         RedirectAttributes ra,
                         @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {

        if (loginUser == null) {
            // 로그인 필요 처리: 로그인 페이지로 리다이렉트하거나 401 처리
            ra.addFlashAttribute("msg", "로그인이 필요합니다.");
            return "redirect:/login";
        }

        if (!form.isDateRangeValid()) {
            br.rejectValue("endDate", "dateRange", "종료일은 시작일 이후여야 합니다.");
        }
        if (br.hasErrors()) {
            return "trips/new";
        }

        TripPlan plan = tripService.createPlan(form.getTitle(), form.getStartDate(), form.getEndDate(), loginUser);

        // 초기 경유지 입력했다면 함께 저장
        if (form.hasInitialStop()) {
            TripStopCreateDTO stopDto = new TripStopCreateDTO(
                    form.getInitialDayOrder() != null ? form.getInitialDayOrder() : 1,
                    form.getInitialPlaceName(),
                    form.getInitialAddress(),
                    form.getInitialMemo(),
                    form.getInitialCost() != null ? form.getInitialCost() : BigDecimal.ZERO
            );
            tripService.addStop(plan.getId(), stopDto, loginUser);
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

        boolean isOwner = false;
        if (loginUser != null && plan.getOwner() != null) {
            isOwner = loginUser.getId().equals(plan.getOwner().getId());
        }

        model.addAttribute("plan", plan);
        model.addAttribute("stops", stops);
        model.addAttribute("stopForm", new TripStopCreateDTO(1,"","","", BigDecimal.ZERO));
        model.addAttribute("totalCost", totalCost);
        model.addAttribute("dayCost", dayCost);
        model.addAttribute("isOwner", isOwner);

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
        // 필요 시 소유자 검증을 서비스로 위임(현재 removeStop은 권한검사 없이 삭제)
        tripService.removeStop(stopId);
    }

    @PatchMapping("/stops/{stopId}/day")
    @ResponseBody
    public void moveDay(@PathVariable("stopId") Long stopId,
                        @RequestParam("newDay") int newDay,
                        @SessionAttribute(name = LOGIN_USER_ATTR, required = false) User loginUser) {
        tripService.moveDay(stopId, newDay);
    }
}