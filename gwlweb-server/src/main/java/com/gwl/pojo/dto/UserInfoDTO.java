package com.gwl.pojo.dto;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoDTO implements Serializable{
    private Long id;

    private String username;

    private String sex;

    private String avatarurl;
}
