package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "user_mission")
public class UserMissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_mission_id", nullable = false)
    private Long userMissionId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean expired = false;


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}
