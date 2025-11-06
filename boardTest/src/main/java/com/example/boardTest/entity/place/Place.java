package com.example.boardTest.entity.place;

import com.example.boardTest.entity.Base;
import com.example.boardTest.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "places")
public class Place extends Base {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String name;

    @Column(length=200)
    private String address;

    @Column(length=40)
    private String category;
    private Integer rating;

    @Column(length=500)
    private String imageUrl;

    @Lob
    private String memo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="author_id")
    private User author;
}