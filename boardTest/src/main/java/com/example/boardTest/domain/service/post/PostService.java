package com.example.boardTest.domain.service.post;

import com.example.boardTest.domain.entity.board.ContentType;
import com.example.boardTest.domain.entity.board.Post;
import com.example.boardTest.domain.entity.User;
import com.example.boardTest.domain.repository.post.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository repo;

    // --- 조회 공통 ---
    public Post findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다. id=" + id));
    }

    public Page<Post> findPage(int page, int size) {
        return repo.findAll(pageable(page, size));
    }

    public Page<Post> search(String keyword, int page, int size) {
        return repo.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                keyword, keyword, pageable(page, size));
    }

    public Page<Post> findByType(ContentType type, int page, int size) {
        return repo.findByType(type, pageable(page, size));
    }

    public Page<Post> findByTypeAndPlatform(ContentType type, String platform, int page, int size) {
        return repo.findByTypeAndPlatformIgnoreCase(type, platform, pageable(page, size));
    }

    // --- 생성/수정/삭제 ---
    @Transactional
    public Post create(String title, String content, User author,
                       ContentType type, String platform, LocalDate watchedAt,
                       String tags, String imageUrl) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .type(type)
                .platform(platform)
                .watchedAt(watchedAt)
                .tags(tags)
                .imageUrl(imageUrl)
                .build();
        return repo.save(post);
    }

    @Transactional
    public Post update(Long id, String title, String content, User editor,
                       ContentType type, String platform, LocalDate watchedAt,
                       String tags, @Nullable String imageUrl) {

        Post post = findById(id);

        if (post.getAuthor() == null || !post.getAuthor().getId().equals(editor.getId())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }

        post.setTitle(title);
        post.setContent(content);
        post.setType(type);
        post.setPlatform(platform);
        post.setWatchedAt(watchedAt);
        post.setTags(tags);
        if (imageUrl != null && !imageUrl.isBlank()) {
            post.setImageUrl(imageUrl);
        }

        return repo.save(post);
    }

    @Transactional
    public void delete(Long id, User requester) {
        Post post = findById(id);
        if (post.getAuthor() == null || !post.getAuthor().getId().equals(requester.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }
        repo.delete(post);
    }

    // --- 내부 유틸 ---
    private Pageable pageable(int page, int size) {
        int p = Math.max(0, page);
        return PageRequest.of(p, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /** 전체 목록 (최신순) */
    public List<Post> findAll() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    /** 타입별 전체 목록 (최신순) */
    public List<Post> findAllByType(ContentType type) {
        // PageRequest를 사용해 정렬만 걸고 전부 가져오기
        Page<Post> page = repo.findByType(type, PageRequest.of(0, Integer.MAX_VALUE,
                Sort.by(Sort.Direction.DESC, "createdAt")));
        return page.getContent();
    }

    /** 최근 N개 (최신순) */
    public List<Post> findRecent(int limit) {
        return repo.findAll(PageRequest.of(0, Math.max(1, limit),
                Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    public List<Post> search(String keyword) {
        return repo.findByTitleContainingIgnoreCase(keyword);
    }
}