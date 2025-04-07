package com.jhworld.catcash.entity;

import jakarta.persistence.*;
import lombok.*;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "chat_prompt")
public class ChatPromptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prompt_id", nullable = false)
    private Long promptId;

    @Column(nullable = false)
    private String content;

    // 여기에 프롬프트 고도화를 위한 별도 항목 추가


    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @OneToOne
    @JoinColumn(name = "user_cat_id", nullable = false)
    private UserCatEntity userCat;
}
