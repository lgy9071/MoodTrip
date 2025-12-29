package com.example.boardTest.global.utils;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티에서 공통으로 사용하는 기본 엔티티 클래스
 * - 생성일(createdAt), 수정일(updatedAt)을 자동 관리
 * - 상속받은 엔티티의 컬럼으로 포함됨
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class Base {

    /**
     * 엔티티가 최초 저장될 때 자동으로 저장되는 생성 시간
     * - @CreatedDate: JPA Auditing 기능 사용
     * - updatable = false: 수정 시 값 변경 불가
     */
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /**
     * 엔티티가 수정될 때마다 자동으로 갱신되는 수정 시간
     * - @LastModifiedDate: JPA Auditing 기능 사용
     */
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
