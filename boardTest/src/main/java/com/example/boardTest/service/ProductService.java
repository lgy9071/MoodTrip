package com.example.boardTest.service;

import com.example.boardTest.entity.Product;
import com.example.boardTest.entity.User;
import com.example.boardTest.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repo;

    public Page<Product> list(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (keyword == null || keyword.isBlank()) return repo.findAll(pageable);
        return repo.findByCategoryOrName(keyword, keyword, pageable);
    }

    public Product find(Long id){
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException("상품 없음"));
    }

    public Product create(String name, Integer price, String category, String imageUrl, String buyLink, User author) {
        Product p = Product.builder().name(name).price(price).category(category)
                .imageUrl(imageUrl).buyLink(buyLink).author(author).build();
        return repo.save(p);
    }

    public Product update(Long id, String name, Integer price, String category, String imageUrl, String buyLink, User editor){
        Product p = find(id);
        if(!p.getAuthor().getId().equals(editor.getId())) throw new IllegalStateException("작성자만 수정");
        p.setName(name); p.setPrice(price); p.setCategory(category); p.setImageUrl(imageUrl); p.setBuyLink(buyLink);
        return repo.save(p);
    }

    public void delete(Long id, User requester){
        Product p = find(id);
        if(!p.getAuthor().getId().equals(requester.getId())) throw new IllegalStateException("작성자만 삭제");
        repo.delete(p);
    }
}