package com.example.boardTest.service;

import com.example.boardTest.entity.Post;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository repo;

    public List<Post> findAll() {
        return repo.findAll();
    }

    public Post findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 없습니다."));
    }

    public Post create(String title, String content, User author) {
        Post post = Post.builder()
                .title(title)
                .content(content)
                .author(author)
                .build();
        return repo.save(post);
    }

    public Post update(Long id, String title, String content, User editor) {
        Post post = findById(id);
        if (!post.getAuthor().getId().equals(editor.getId())) {
            throw new IllegalStateException("작성자만 수정할 수 있습니다.");
        }
        post.setTitle(title);
        post.setContent(content);
        return repo.save(post);
    }

    public void delete(Long id, User requester) {
        Post post = findById(id);
        if (!post.getAuthor().getId().equals(requester.getId())) {
            throw new IllegalStateException("작성자만 삭제할 수 있습니다.");
        }
        repo.delete(post);
    }

    public Page<Post> findPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findAll(pageable);
    }

    public Page<Post> search(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return repo.findByTitleOrContent(keyword, keyword, pageable);
    }
}