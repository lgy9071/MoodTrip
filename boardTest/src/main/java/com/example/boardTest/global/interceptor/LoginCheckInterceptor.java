package com.example.boardTest.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인 여부를 검사하는 인터셉터
 * - 컨트롤러 실행 전에 호출됨
 * - 세션에 로그인 정보가 없으면 로그인 페이지로 리다이렉트
 */
public class LoginCheckInterceptor implements HandlerInterceptor {

    /**
     * 세션에 저장된 로그인 사용자 정보를 꺼낼 때 사용하는 key
     * (로그인 성공 시 세션에 동일한 이름으로 저장되어 있어야 함)
     */
    public static final String LOGIN_USER_ATTR = "LOGIN_USER";

    /**
     * 컨트롤러 실행 전에 호출되는 메서드
     *
     * @return true  → 컨트롤러 정상 실행
     *         false → 요청 중단 (컨트롤러로 가지 않음)
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 기존 세션이 있으면 가져오고, 없으면 새로 생성하지 않음
        HttpSession session = request.getSession(false);

        // 세션이 존재하면 로그인 사용자 정보 조회, 없으면 null
        Object loginUser = (session != null)
                ? session.getAttribute(LOGIN_USER_ATTR)
                : null;

        // 로그인 정보가 없으면 (로그인하지 않은 상태)
        if (loginUser == null) {

            // 사용자가 원래 요청한 URI (로그인 후 돌아가기 위해 저장)
            String requestURI = request.getRequestURI();

            // 로그인 페이지로 리다이렉트
            // next 파라미터로 원래 요청 URL 전달
            response.sendRedirect("/login?next=" + requestURI);

            // false 반환 → 컨트롤러 실행되지 않음
            return false;
        }

        // 로그인된 상태 → 요청을 컨트롤러로 전달
        return true;
    }
}
