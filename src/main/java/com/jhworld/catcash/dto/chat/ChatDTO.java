package com.jhworld.catcash.dto.chat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatDTO {
    private Long chatId;
    private String content;
    private LocalDateTime chatDate;
    private String role;

    public ChatDTO(String content, String role) {
        this.chatDate = LocalDateTime.now();
    }
}

