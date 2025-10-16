package com.example.boardTest.controller;

import com.example.boardTest.entity.board.ContentType;
import com.example.boardTest.entity.board.Post;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) ContentType type,
                       @RequestParam(required = false) String platform,
                       Model model) {
        page = Math.max(0, page);

        Page<Post> postPage;
        if (type != null && platform != null) {
            postPage = postService.findByTypeAndPlatform(type, platform, page, 5);
        } else if (type != null) {
            postPage = postService.findByType(type, page, 5);
        } else {
            postPage = postService.findPage(page, 5);
        }

        int totalPages = Math.max(1, postPage.getTotalPages());
        if (page > 0 && page >= totalPages) {
            int last = Math.max(0, totalPages - 1);
            return "redirect:/posts?page=" + last;
        }

        model.addAttribute("postPage", postPage);
        model.addAttribute("currentPage", postPage.getNumber());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedPlatform", platform);

        // enum 배열 직접 내려주기 (T(...) 제거)
        model.addAttribute("types", ContentType.values());

        return "posts/list";
    }

    @GetMapping("/search")
    public String search(@RequestParam("keyword") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        page = Math.max(0, page);
        Page<Post> postPage = postService.search(keyword, page, 5);

        int totalPages = Math.max(1, postPage.getTotalPages());
        if (page > 0 && page >= totalPages) {
            int last = Math.max(0, totalPages - 1);
            return "redirect:/posts/search?keyword=" + UriUtils.encode(keyword, StandardCharsets.UTF_8) + "&page=" + last;
        }

        model.addAttribute("postPage", postPage);
        model.addAttribute("currentPage", postPage.getNumber());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        return "posts/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "posts/detail";
    }

    @GetMapping("/new")
    public String form(HttpSession session, Model model) {
        requireLogin(session);
        model.addAttribute("post", null);
        model.addAttribute("types", ContentType.values());
        return "posts/form";
    }

    @PostMapping
    public String create(@RequestParam("title") String title,
                         @RequestParam("content") String content,
                         @RequestParam("type") ContentType type,
                         @RequestParam(required = false) String platform,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate watchedAt,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String imageUrl, // (간단 버전) 파일업로드 미도입
                         HttpSession session) {
        User user = requireLogin(session);
        Post post = postService.create(title, content, user, type, platform, watchedAt, tags, imageUrl);
        return "redirect:/posts/" + post.getId();   // 리다이렉트
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        User user = requireLogin(session);
        Post post = postService.findById(id);
        if (post.getAuthor() == null || !post.getAuthor().getId().equals(user.getId())) {
            model.addAttribute("post", post);
            model.addAttribute("error", "작성자만 수정할 수 있습니다.");
            return "posts/detail";
        }
        model.addAttribute("post", post);
        model.addAttribute("types", ContentType.values());
        return "posts/form";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("title") String title,
                         @RequestParam("content") String content,
                         @RequestParam("type") ContentType type,
                         @RequestParam(required = false) String platform,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate watchedAt,
                         @RequestParam(required = false) String tags,
                         @RequestParam(required = false) String imageUrl,
                         HttpSession session,
                         Model model) {
        User user = requireLogin(session);
        try {
            postService.update(id, title, content, user, type, platform, watchedAt, tags, imageUrl);
            return "redirect:/posts/" + id;
        } catch (Exception e) {
            Post post = postService.findById(id);
            model.addAttribute("post", post);
            model.addAttribute("types", ContentType.values());
            model.addAttribute("error", e.getMessage());
            return "posts/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpSession session, Model model) {
        User user = requireLogin(session);
        try {
            postService.delete(id, user);
            return "redirect:/posts";
        } catch (Exception e) {
            model.addAttribute("post", postService.findById(id));
            model.addAttribute("error", e.getMessage());
            return "posts/detail";
        }
    }

    private User requireLogin(HttpSession session) {
        User user = AuthController.currentUser(session);
        if (user == null) throw new IllegalStateException("로그인이 필요합니다.");
        return user;
    }
}