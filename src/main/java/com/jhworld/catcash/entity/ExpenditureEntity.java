package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "expenditure")
public class ExpenditureEntity {
    @Id
    @Column(name = "expenditure_id", nullable = false)
    private Long expenditureId;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false)
    private Long amount;

    @Column(nullable = false)
    private Long payment;

    @Column(nullable = false)
    private String memo;

    @Column(name = "budget_including", nullable = false)
    private Boolean budgetIncluding;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;
}
