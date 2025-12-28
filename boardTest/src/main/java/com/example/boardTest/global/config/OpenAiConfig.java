package com.example.boardTest.global.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAI API 클라이언트 설정 클래스
 *
 * - OpenAI 공식 Java SDK(Client) Bean 등록
 * - API Key를 환경변수(.env / application.yml)에서 안전하게 주입
 * - Service 계층에서 바로 주입받아 사용 가능
 */
@Configuration
public class OpenAiConfig {

    /**
     * OPENAI_API_KEY
     *
     * - application.yml / application.properties / .env 에 정의
     * - 소스 코드에 직접 키를 하드코딩하지 않기 위함
     */
    @Value("${OPENAI_API_KEY}")
    private String apiKey;

    /**
     * OpenAIClient Bean 등록
     *
     * Spring 실행 시 한 번 생성되어
     * 애플리케이션 전역에서 재사용됨 (Singleton)
     */
    @Bean
    public OpenAIClient openAIClient() {

        // OkHttp 기반 OpenAI Client 생성
        return OpenAIOkHttpClient.builder()

                // OpenAI API 인증 키 설정
                // 실제 요청 시 Authorization 헤더에 사용됨
                .apiKey(apiKey)

                // Client 생성 완료
                .build();
    }
}
