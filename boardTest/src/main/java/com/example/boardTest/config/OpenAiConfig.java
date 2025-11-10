package com.example.boardTest.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAiConfig {

    @Bean
    public com.openai.client.OpenAIClient openAIClient() {
        // 환경변수(OPENAI_API_KEY)를 자동으로 읽어 초기화
        return com.openai.client.okhttp.OpenAIOkHttpClient.fromEnv();
    }
}