package com.example.boardTest.repository;

import com.example.boardTest.entity.board.ContentType;
import com.example.boardTest.entity.board.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findAll(Pageable pageable);

    // 제목/내용 부분일치 + 대소문자 무시
    Page<Post> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String titleKeyword, String contentKeyword, Pageable pageable
    );

    // 타입/플랫폼 필터
    Page<Post> findByType(ContentType type, Pageable pageable);

    Page<Post> findByTypeAndPlatformIgnoreCase(ContentType type, String platform, Pageable pageable);
}