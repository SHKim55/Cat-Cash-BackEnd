package com.jhworld.catcash.dto.mission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserMissionDTO {
    private Long userMissionId;
    private String title;
    private String content;
    private Boolean completed;
    private Boolean expired;
}
