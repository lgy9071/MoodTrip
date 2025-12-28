package com.example.boardTest.global.config;

import com.example.boardTest.global.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 인터셉터 설정 클래스
 *
 * - 특정 URL 접근 전에 로그인 여부 검사
 * - Controller 실행 전에 공통 로직 수행
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 인터셉터 등록
     *
     * 요청 흐름:
     *  브라우저 요청
     *   → LoginCheckInterceptor.preHandle()
     *   → (통과 시) Controller
     *   → View / JSON 응답
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginCheckInterceptor())

                // 로그인 검사가 필요한 URL 패턴
                // 여행 / 장소 / 리뷰 관련 기능은
                // 로그인 사용자만 접근 가능
                .addPathPatterns(
                        "/trips/**",
                        "/places/**",
                        "/reviews/**"
                )

                // 로그인 검사 제외 대상
                // (비로그인 사용자도 접근 가능)
                .excludePathPatterns(
                        "/",                 // 메인 페이지
                        "/login", "/signup", // 로그인 / 회원가입
                        "/css/**",           // 정적 리소스
                        "/js/**",
                        "/images/**",
                        "/favicon.ico",
                        "/error"             // Spring 기본 에러 페이지
                );
    }
}
