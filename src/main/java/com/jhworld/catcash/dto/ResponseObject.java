package com.jhworld.catcash.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseObject {
    private Integer code;
    private String message;
    private Object data;
}
