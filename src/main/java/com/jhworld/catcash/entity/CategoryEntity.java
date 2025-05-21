package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "category")
public class CategoryEntity {
    @Id
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

//    @Column(name = "`group`", nullable = false)
//    private String group;

    @Column(nullable = false)
    private String category;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<UserCategoryEntity> userCategories;
}