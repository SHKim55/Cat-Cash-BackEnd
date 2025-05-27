package com.jhworld.catcash.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.jhworld.catcash.service.llm.GptRole;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GptRequest {
    private String model;
    private Double temperature;
    @JsonProperty("max_tokens")
    private Integer maxTokens;
    private List<Message> messages;

    @Data
    @AllArgsConstructor
    public static class Message {
        private GptRole role;
        private String content;
    }
}
