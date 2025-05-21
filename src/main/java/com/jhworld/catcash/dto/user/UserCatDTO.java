package com.jhworld.catcash.dto.user;

import com.jhworld.catcash.entity.UserCatEntity;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class UserCatDTO {
    private Long userCatId;
    private String customName;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private Long exp;

    public static UserCatDTO convertEntityToDTO(UserCatEntity userCatEntity) {
        return UserCatDTO.builder()
                .userCatId(userCatEntity.getUserCatId())
                .customName(userCatEntity.getCustomName())
                .createdTime(userCatEntity.getCreatedTime())
                .modifiedTime(userCatEntity.getModifiedTime())
                .exp(userCatEntity.getExp())
                .build();
    }
}