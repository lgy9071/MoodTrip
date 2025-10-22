package com.example.boardTest.controller;

import com.example.boardTest.entity.User;
import com.example.boardTest.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    public static final String LOGIN_USER = "LOGIN_USER";

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam("username") String username,
                         @RequestParam("password") String password,
                         @RequestParam("name") String name,
                         @RequestParam("email") String email,
                         Model model) {
        try {
            userService.register(username, password, name, email);
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "signup"; // 경로 통일
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam("username") String username,
                        @RequestParam("password") String password,
                        @RequestParam(value = "next", required = false) String next,
                        HttpSession session,
                        Model model) {
        try {
            User u = userService.login(username, password);
            session.setAttribute(LOGIN_USER, u);

            // next 파라미터가 있으면 해당 경로로 리다이렉트
            if (next != null && !next.isBlank()) {
                return "redirect:" + next;
            }

            // 기본 리다이렉트 경로
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            // 로그인 폼에서 next 파라미터 유지 (로그인 실패 시에도 다시 전달)
            model.addAttribute("next", next);
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes ra) {
        session.invalidate();
        ra.addAttribute("loggedOut", "true");   // 메인에서 안내 띄우기 위함
        return "redirect:/";
    }

    public static User currentUser(HttpSession session) {
        return (User) session.getAttribute(LOGIN_USER);
    }
}
