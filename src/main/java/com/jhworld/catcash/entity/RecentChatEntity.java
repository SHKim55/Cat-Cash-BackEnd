package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "recentChat")
public class RecentChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chat_id", nullable = false)
    private Long chatId;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @ManyToOne
    @JoinColumn(name = "user_cat_id", nullable = false)
    private UserCatEntity userCat;

    @Column(name = "embedding", columnDefinition = "TEXT", nullable = false)
    private String embedding;
}
