package com.gwl.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String emailaddress;

    private String username;

    private String password;

    private int sex;

    private int status;

    private String avatarurl;

    private int age;

    private Instant createTime;

    private Instant updateTime;

}
