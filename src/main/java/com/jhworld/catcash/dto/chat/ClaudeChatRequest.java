package com.jhworld.catcash.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor
public class ClaudeChatRequest {
    private String model;
    private Integer max_tokens;
    @JsonProperty("system")
    private String systemPrompt;
    private List<ClaudeMessage> messages;
}