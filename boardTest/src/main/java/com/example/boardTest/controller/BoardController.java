package com.example.boardTest.controller;

import com.example.boardTest.entity.Post;
import com.example.boardTest.entity.User;
import com.example.boardTest.service.PostService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final PostService postService;

    @GetMapping("/board")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        page = Math.max(0, page);

        Page<Post> postPage = postService.findPage(page, 5);

        int totalPages = postPage.getTotalPages();
        if (page > 0 && page >= totalPages) {
            int last = Math.max(0, totalPages - 1);
            return "redirect:/board?page=" + last;
        }

        model.addAttribute("postPage", postPage);
        model.addAttribute("currentPage", postPage.getNumber());
        model.addAttribute("totalPages", totalPages);
        return "board/list";
    }

    @GetMapping("/board/search")
    public String search(@RequestParam("keyword") String keyword,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        page = Math.max(0, page);

        Page<Post> postPage = postService.search(keyword, page, 5);

        int totalPages = postPage.getTotalPages();
        if (page > 0 && page >= totalPages) {
            int last = Math.max(0, totalPages - 1);
            return "redirect:/board/search?keyword=" + keyword + "&page=" + last;
        }

        model.addAttribute("postPage", postPage);
        model.addAttribute("currentPage", postPage.getNumber());
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("keyword", keyword);
        return "board/list";
    }

    @GetMapping("/board/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("post", postService.findById(id));
        return "board/detail";
    }

    @GetMapping("/board/new")
    public String form(HttpSession session, Model model) {
        requireLogin(session);
        return "board/form";
    }

    @PostMapping("/board")
    public String create(@RequestParam("title") String title,
                         @RequestParam("content") String content,
                         HttpSession session) {
        User user = requireLogin(session);
        Post post = postService.create(title, content, user);
        return "board/" + post.getId();
    }

    @GetMapping("/board/{id}/edit")
    public String editForm(@PathVariable("id") Long id, HttpSession session, Model model) {
        User user = requireLogin(session);
        Post post = postService.findById(id);

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(user.getId())) {
            model.addAttribute("post", post);
            model.addAttribute("error", "작성자만 수정할 수 있습니다.");
            return "board/detail";
        }

        model.addAttribute("post", post);
        return "board/form";
    }

    @PostMapping("/board/{id}/edit")
    public String update(@PathVariable("id") Long id,
                         @RequestParam("title") String title,
                         @RequestParam("content") String content,
                         HttpSession session,
                         Model model) {
        User user = requireLogin(session);
        try {
            postService.update(id, title, content, user);
            return "redirect:/board/" + id;
        } catch (Exception e) {
            Post post = postService.findById(id);
            model.addAttribute("post", post);
            model.addAttribute("error", e.getMessage());
            return "board/detail";
        }
    }

    @PostMapping("/board/{id}/delete")
    public String delete(@PathVariable("id") Long id,
                         HttpSession session,
                         Model model) {
        User user = requireLogin(session);
        try {
            postService.delete(id, user);
            return "redirect:/board";
        } catch (Exception e) {
            model.addAttribute("post", postService.findById(id));
            model.addAttribute("error", e.getMessage());
            return "board/detail";
        }
    }

    private User requireLogin(HttpSession session) {
        User user = AuthController.currentUser(session);
        if (user == null) throw new IllegalStateException("로그인이 필요합니다.");
        return user;
    }
}