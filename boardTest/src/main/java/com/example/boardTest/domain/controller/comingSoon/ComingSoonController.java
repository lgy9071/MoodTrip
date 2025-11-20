package com.example.boardTest.domain.controller.comingSoon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComingSoonController {
    @GetMapping("/coming-soon")
    public String comingSoon() {
        return "common/coming-soon";
    }
}