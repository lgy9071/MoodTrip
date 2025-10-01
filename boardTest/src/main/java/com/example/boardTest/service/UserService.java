package com.example.boardTest.service;


import com.example.boardTest.entity.User;
import com.example.boardTest.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User register(String username, String password, String name, String email) {
        repo.findByUsername(username).ifPresent(u -> {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        });
        repo.findByEmail(email).ifPresent(u -> {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        });

        User user = User.builder()
                .username(username)
                .password(encoder.encode(password))
                .name(name)
                .email(email)
                .emailVerified(false)
                .build();

        return repo.save(user);
    }

    public User login(String username, String password) {
        User user = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 아이디입니다."));

        if (!encoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }
        return user;
    }
}