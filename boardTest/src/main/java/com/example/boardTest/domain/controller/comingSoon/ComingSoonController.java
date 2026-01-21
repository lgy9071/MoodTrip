package com.example.boardTest.domain.controller.comingSoon;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ComingSoonController {

    /**
     * 서비스 준비중 페이지
     * 단순 정적 View 반환용 컨트롤러
     */
    @GetMapping("/coming-soon")
    public String comingSoon() {
        // templates/common/coming-soon.html
        return "common/coming-soon";
    }
}