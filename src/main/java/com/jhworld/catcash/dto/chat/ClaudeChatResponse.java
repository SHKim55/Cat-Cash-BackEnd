package com.jhworld.catcash.dto.chat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClaudeChatResponse {
    private List<ContentBlock> content;

    public List<ContentBlock> getContent() {
        return content;
    }
    public void setContent(List<ContentBlock> content) {
        this.content = content;
    }
}

