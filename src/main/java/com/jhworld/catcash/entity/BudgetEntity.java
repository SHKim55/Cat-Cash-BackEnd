package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "budget")
public class BudgetEntity {
    @Id
    @Column(name = "budget_id", nullable = false)
    private Long budgetId;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
}
