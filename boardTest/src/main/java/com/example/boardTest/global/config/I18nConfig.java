package com.example.boardTest.global.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.util.Locale;

/**
 * 다국어(i18n) 설정 클래스
 * - 요청 파라미터로 언어 변경
 * - 세션 기반 Locale 유지
 * - messages.properties 파일 로딩
 */
@Configuration
public class I18nConfig implements WebMvcConfigurer {

    /**
     * LocaleResolver
     * - 현재 사용자의 Locale(언어/국가)를 결정하는 역할
     * - SessionLocaleResolver → 세션에 Locale 저장
     * - 최초 접속 시 기본 Locale은 한국어(ko)
     */
    @Bean
    public LocaleResolver localeResolver() {
        SessionLocaleResolver r = new SessionLocaleResolver();

        // 사용자가 언어를 선택하지 않았을 경우 기본 언어
        r.setDefaultLocale(Locale.KOREAN); // ko_KR

        return r;
    }

    /**
     * LocaleChangeInterceptor
     * - 요청 파라미터로 Locale 변경을 감지하는 인터셉터
     * - 예: /board/list?lang=en
     *       → Locale = ENGLISH
     * - 변경된 Locale은 LocaleResolver(Session)에 저장됨
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor i = new LocaleChangeInterceptor();

        // 언어 변경에 사용할 파라미터 이름
        // ?lang=ko | ?lang=en | ?lang=uk
        i.setParamName("lang");

        return i;
    }

    /**
     * 인터셉터 등록
     * - 모든 요청에 대해 LocaleChangeInterceptor 실행
     * - Controller 호출 전에 lang 파라미터가 있는지 확인
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }

    /**
     * MessageSource
     * - 다국어 메시지 파일을 읽어오는 역할
     * - messages_ko.properties
     * - messages_en.properties
     * - ValidationMessages_ko.properties 등
     */
    @Bean
    public MessageSource messageSource() {
        ResourceBundleMessageSource ms = new ResourceBundleMessageSource();

        // resources 하위에서 다음 파일들을 찾음
        // messages.properties
        // messages_ko.properties
        // messages_en.properties
        // ValidationMessages.properties ...
        ms.setBasenames("messages", "ValidationMessages");

        // 한글 깨짐 방지
        ms.setDefaultEncoding("UTF-8");

        // 시스템 Locale 자동 사용 방지
        ms.setFallbackToSystemLocale(false);

        return ms;
    }
}
