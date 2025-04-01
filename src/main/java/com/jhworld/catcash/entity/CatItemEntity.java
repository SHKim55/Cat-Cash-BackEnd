package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "cat_item")
public class CatItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cat_item_id", nullable = false)
    private Long catItemId;


    @ManyToOne
    @JoinColumn(name = "cat_id", nullable = false)
    private CatEntity cat;

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private ItemEntity item;
}
