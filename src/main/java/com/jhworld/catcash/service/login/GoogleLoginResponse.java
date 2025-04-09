package com.jhworld.catcash.service.login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class GoogleLoginResponse {
    private String accessToken;
    private String tokenType;
    private Long expiresIn;
    private String refreshToken;
}