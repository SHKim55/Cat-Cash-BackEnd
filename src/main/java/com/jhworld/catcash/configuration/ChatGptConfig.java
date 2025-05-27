package com.jhworld.catcash.configuration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatGptConfig {
    // ChatGPT
    public static final String DEFAULT_MODEL = "gpt-4-turbo";
//    public static final String AUTHORIZATION = "Authorization";
//    public static final String BEARER_TOKEN = "Bearer ";
    public static final String BASE_URL = "https://api.openai.com/v1/chat/completions";
    public static final Double TEMPERATURE = 0.8;
    public static final Integer MAx_TOKENS = 50;
}
