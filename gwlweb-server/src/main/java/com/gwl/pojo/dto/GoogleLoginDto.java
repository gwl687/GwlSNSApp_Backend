package com.gwl.pojo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GoogleLoginDto {
    private String idTokenString;
    private String pushToken;
}
