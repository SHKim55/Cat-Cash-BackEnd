package com.jhworld.catcash.dto.user;

import com.jhworld.catcash.entity.UserEntity;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String userSequence;
    private String username;
    private String email;
    private String profileImageUrl;
    private String gender;
    private LocalDateTime createdTime;
    private LocalDateTime modifiedTime;
    private Long income;
    private Long fixedExpenditure;
    private Long savingProportion;
    private String expenseType;
    private Boolean isNew;

    public static UserDTO convertEntityToDTO(final UserEntity userEntity) {
        return UserDTO.builder()
                .userId(userEntity.getUserId())
                .userSequence(userEntity.getUserSequence())
                .username(userEntity.getUsername())
                .email(userEntity.getEmail())
                .profileImageUrl(userEntity.getProfileImageUrl())
                .gender(userEntity.getGender())
                .createdTime(userEntity.getCreatedTime())
                .modifiedTime(userEntity.getModifiedTime())
                .income(userEntity.getIncome())
                .fixedExpenditure(userEntity.getFixedExpenditure())
                .savingProportion(userEntity.getSavingProportion())
                .expenseType(userEntity.getExpenseType())
                .isNew(userEntity.getIsNew())
                .build();
    }

    public static UserEntity convertDTOToEntity(final UserDTO userDTO) {
        return UserEntity.builder()
                .userId(userDTO.getUserId())
                .userSequence(userDTO.getUserSequence())
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .profileImageUrl(userDTO.getProfileImageUrl())
                .gender(userDTO.getGender())
                .createdTime(userDTO.getCreatedTime())
                .modifiedTime(userDTO.getModifiedTime())
                .income(userDTO.getIncome())
                .fixedExpenditure(userDTO.getFixedExpenditure())
                .savingProportion(userDTO.getSavingProportion())
                .expenseType(userDTO.getExpenseType())
                .isNew(userDTO.getIsNew())
                .build();
    }
}
