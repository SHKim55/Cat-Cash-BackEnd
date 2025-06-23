package com.jhworld.catcash.service.fcm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PersonalMessageData {
    private String username;
    private String mainCategory;
}
