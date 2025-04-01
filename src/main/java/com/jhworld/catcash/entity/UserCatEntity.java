package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "user_cat")
public class UserCatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_cat_id", nullable = false)
    private Long userCatId;

    @Column(name = "custom_name", nullable = false)
    private String customName;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "modified_time", nullable = false)
    private LocalDateTime modifiedTime;

    @Column(nullable = false)
    private Long exp;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "cat_id", nullable = false)
    private CatEntity cat;

    @OneToOne(mappedBy = "userCat", cascade = CascadeType.ALL)
    private ChatPromptEntity chatPrompt;
}
