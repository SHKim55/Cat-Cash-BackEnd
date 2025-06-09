package com.jhworld.catcash.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationDTO {
    private String token;
    private String title;
    private String body;

    public static PushNotificationDTO of(String token, String title, String body) {
        return PushNotificationDTO.builder()
                .token(token)
                .title(title)
                .body(body)
                .build();
    }
}
