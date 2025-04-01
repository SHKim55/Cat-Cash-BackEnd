package com.jhworld.catcash.entity;

import com.jhworld.catcash.service.item.ItemType;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "item")
public class ItemEntity {
    @Id
    @Column(name = "item_id", nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ItemType type;

    @Column(nullable = false)
    private Long level;

    @Column(name = "image_id", nullable = false)
    private Long imageId;


    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<CatItemEntity> catItems;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<InventoryEntity> inventories;
}
