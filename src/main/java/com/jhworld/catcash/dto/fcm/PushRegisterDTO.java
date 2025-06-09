package com.jhworld.catcash.dto.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PushRegisterDTO {
    private Long userId;
    private String deviceToken;
}
