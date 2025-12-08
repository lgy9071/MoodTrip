package com.example.boardTest.global.config;

import com.example.boardTest.global.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/trips/**", "/places/**", "/reviews/**")   // 보호할 URI
                .excludePathPatterns(
                        "/",                 // 메인
                        "/login", "/signup", // 인증 관련
                        "/css/**", "/js/**", "/images/**", "/favicon.ico",
                        "/error"             // 오류 페이지
                );
    }
}
