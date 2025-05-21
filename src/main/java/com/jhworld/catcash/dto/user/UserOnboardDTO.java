package com.jhworld.catcash.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserOnboardDTO {
    private String username;
    private Long income;
    private Long fixedExpenditure;
    private Long savingProportion;
    private List<Long> categoryIdList;
    private String expenseType;

    // 고양이 이름 머냥으로 통일
    // 소비타입 - 충동형, 절약형, 루틴형, 무계획형
}
