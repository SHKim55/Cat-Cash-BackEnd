package com.jhworld.catcash.entity.pg;

import com.jhworld.catcash.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "vectorChat")
public class VectorChatEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long _id;

    @Column(columnDefinition = "vector")
    private float[] embedding;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "content")
    private String content;
}

