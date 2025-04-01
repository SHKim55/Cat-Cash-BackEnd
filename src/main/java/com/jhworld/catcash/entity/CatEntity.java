package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "cat")
public class CatEntity {
    @Id
    @Column(name = "cat_id", nullable = false)
    private Long catId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long level;

    @Column(name = "image_id", nullable = false)
    private String imageId;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;


    @OneToMany(mappedBy = "cat", cascade = CascadeType.ALL)
    private List<UserCatEntity> userCats;

    @OneToMany(mappedBy = "cat", cascade = CascadeType.ALL)
    private List<CatItemEntity> catItems;
}
