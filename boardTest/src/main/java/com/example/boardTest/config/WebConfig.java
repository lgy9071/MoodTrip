package com.example.boardTest.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 정적 리소스 매핑 (Trip 이미지 추가할때 추가)
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 업로드된 파일을 /uploads/** 로 서빙
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}