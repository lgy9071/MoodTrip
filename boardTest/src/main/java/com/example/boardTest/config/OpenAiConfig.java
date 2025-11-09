package com.example.boardTest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenAiConfig {

    @Value("${openai.apiKey}")
    private String apiKey; // 환경변수 → yml → 여기로

    @Bean
    public com.openai.client.OpenAIClient openAIClient() {
        return com.openai.client.okhttp.OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .build();
    }
}