package com.jhworld.catcash.controller;

import com.jhworld.catcash.dto.chat.ChatDTO;
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
    public ResponseEntity<ChatDTO> createMessage(@RequestHeader(name = "Authorization") String token, String userInput) {
        return chatService.createMessage(token, userInput);
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
