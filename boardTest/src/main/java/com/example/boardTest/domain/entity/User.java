package com.example.boardTest.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User {

    // 사용자 PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 로그인 아이디
    @Column(nullable = false, unique = true, length = 30)
    private String username;

    // 비밀번호 (암호화 전제)
    @Column(nullable = false)
    private String password;

    // 사용자 이름
    @Column(nullable = false, length = 50)
    private String name;

    // 이메일
    @Column(nullable = false, unique = true)
    private String email;

    // 이메일 인증 여부
    @Column(nullable=false, columnDefinition = "boolean default false")
    private boolean emailVerified;
}
