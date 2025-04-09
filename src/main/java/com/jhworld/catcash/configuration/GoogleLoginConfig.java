package com.jhworld.catcash.configuration;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class GoogleLoginConfig {
    @Value("${google.client.id}")
    private String clientId;

    @Value("${google.client.secret}")
    private String clientSecret;

    @Value("${google.page.uri}")
    private String loginPageUrl;

    @Value("${google.redirect.uri}")
    private String redirectUri;

    @Value("${google.access.uri}")
    private String accessTokenUri;

    @Value("${google.profile.uri}")
    private String userProfileUri;

    private final String responseType = "code";

    private final String scope = "email profile";

    private final String grantType = "authorization_code";
}