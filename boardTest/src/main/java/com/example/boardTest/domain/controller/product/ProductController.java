package com.example.boardTest.domain.controller.product;

import com.example.boardTest.domain.entity.product.Product;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.service.product.ProductService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final ProductService service;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        Page<Product> pg = service.list(keyword, page, 8);
        int total = pg.getTotalPages();
        if (page > 0 && page >= total) return "redirect:/products?page=" + Math.max(0, total - 1) + (keyword != null ? "&keyword=" + keyword : "");
        model.addAttribute("page", pg);
        model.addAttribute("currentPage", pg.getNumber());
        model.addAttribute("totalPages", total);
        model.addAttribute("keyword", keyword);
        return "products/list";
    }

    @GetMapping("/new")
    public String form() {
        return "products/form";
    }

    @PostMapping
    public String create(@RequestParam("name") String name,
                         @RequestParam(required = false) Integer price,
                         @RequestParam("category") String category,
                         @RequestParam(required = false) String imageUrl,
                         @RequestParam(required = false) String buyLink,
                         HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        service.create(name, price, category, imageUrl, buyLink, user);
        return "redirect:/products";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {
        model.addAttribute("product", service.find(id));
        return "products/detail";
    }

    @PostMapping("/{id}/edit")
    public String edit(@PathVariable("id") Long id,
                       @RequestParam("name") String name,
                       @RequestParam(required = false) Integer price,
                       @RequestParam("category") String category,
                       @RequestParam(required = false) String imageUrl,
                       @RequestParam(required = false) String buyLink,
                       HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        service.update(id, name, price, category, imageUrl, buyLink, user);
        return "redirect:/products/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable("id") Long id, HttpSession session) {
        User user = (User) session.getAttribute("LOGIN_USER");
        service.delete(id, user);
        return "redirect:/products";
    }
}
