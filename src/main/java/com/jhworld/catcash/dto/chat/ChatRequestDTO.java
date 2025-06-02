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
public class ChatRequestDTO {
    private ChatDTO[] messages;
    private MemoryDTO[] memories;
    private StatusDTO status;
}
