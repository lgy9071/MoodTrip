package com.example.boardTest.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정 클래스
 *
 * - 엔티티의 생성일/수정일/생성자/수정자 등을
 *   자동으로 관리하기 위한 설정
 *
 * - @EnableJpaAuditing 없이는
 *   @CreatedDate, @LastModifiedDate 등이 동작하지 않음
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // 별도의 Bean 정의가 없어도
    // @EnableJpaAuditing 선언만으로 Auditing 기능 활성화
}
