package com.jhworld.catcash.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jhworld.catcash.dto.chat.ChatDTO;
import com.jhworld.catcash.dto.chat.ChatRequestDTO;
import com.jhworld.catcash.dto.chat.ChatResponseDTO;
import com.jhworld.catcash.service.chat.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ChatService chatService;

    public ChatController(ChatService chatService) { this.chatService = chatService; }

    @PostMapping("/new")
    public ResponseEntity<ChatResponseDTO> createMessage(@RequestHeader(name = "Authorization") String token, @RequestBody() ChatRequestDTO chatRequestDTO) throws JsonProcessingException {
        return chatService.createMessage(token, chatRequestDTO);
    }

    @GetMapping("/log")
    public ResponseEntity<List<ChatDTO>> loadMessage(@RequestHeader(name = "Authorization") String token) {
        return chatService.loadMessage(token);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteMessage(@RequestHeader(name = "Authorization") String token, Long chatId) {
        return null;
    }
}
