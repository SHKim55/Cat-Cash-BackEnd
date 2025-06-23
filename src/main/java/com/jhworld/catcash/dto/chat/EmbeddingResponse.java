package com.jhworld.catcash.dto.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EmbeddingResponse {

    @Data
    public static class DataItem {
        @JsonProperty("embedding")
        private List<Float> embedding;
        @JsonProperty("index")
        private int index;
    }

    private String model;
    private List<DataItem> data;
    @JsonProperty("usage")
    private Usage usage;

    @Data
    public static class Usage {
        @JsonProperty("prompt_tokens")
        private int promptTokens;
        @JsonProperty("total_tokens")
        private int totalTokens;
    }
}