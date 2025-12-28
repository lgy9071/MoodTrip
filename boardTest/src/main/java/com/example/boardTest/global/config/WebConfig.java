package com.example.boardTest.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 설정 클래스
 *
 * 정적 리소스(Resource) 매핑 담당
 * - 서버 디스크에 저장된 파일을 URL로 접근 가능하게 함
 *
 * 예:
 *  실제 파일 경로 : uploads/trip/1.jpg
 *  접근 URL       : /uploads/trip/1.jpg
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 정적 리소스 핸들러 추가
     *
     * Spring MVC는 기본적으로
     * - classpath:/static
     * - classpath:/public
     * 만 정적 리소스로 인식함
     *
     * uploads/ 디렉토리는 외부 경로이므로
     * 명시적으로 매핑해줘야 브라우저에서 접근 가능
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // 1 URL 패턴
        // /uploads/** 로 들어오는 모든 요청을
        registry.addResourceHandler("/uploads/**")

                // 2️ 실제 서버 파일 시스템 경로
                // file: 접두어 → OS 파일 시스템
                // 프로젝트 루트 기준 uploads/ 폴더
                .addResourceLocations("file:uploads/");
    }
}
