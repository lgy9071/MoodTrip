package com.example.boardTest.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginCheckInterceptor implements HandlerInterceptor {

    public static final String LOGIN_USER_ATTR = "LOGIN_USER";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        HttpSession session = request.getSession(false);
        Object loginUser = (session != null) ? session.getAttribute(LOGIN_USER_ATTR) : null;

        // 로그인 안 됨 → 로그인 화면으로 redirect
        if (loginUser == null) {
            String requestURI = request.getRequestURI();
            response.sendRedirect("/login?next=" + requestURI);
            return false; // 컨트롤러로 가지 않음
        }

        return true; // 정상적으로 컨트롤러 호출
    }
}
