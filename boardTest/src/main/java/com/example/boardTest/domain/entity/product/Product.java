package com.example.boardTest.domain.entity.product;

import com.example.boardTest.global.utils.Base;
import com.example.boardTest.domain.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 액세서리/고양이 용품
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product extends Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String name;

    private Integer price;

    @Column(length=500)
    private String imageUrl;

    @Column(length=500)
    private String buyLink;

    @Column(length=50)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="author_id")
    private User author;
}