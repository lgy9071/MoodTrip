package com.example.boardTest.domain.repository;


import com.example.boardTest.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * username 기준 사용자 조회
     * - 로그인 처리
     */
    Optional<User> findByUsername(String username);

    /**
     * email 기준 사용자 조회
     * - 회원가입 / 이메일 인증
     */
    Optional<User> findByEmail(String email);
}
