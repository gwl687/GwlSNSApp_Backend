package com.gwl.pojo.entity;

import java.time.Instant;
import java.util.List;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfo {
    private Long id;

    private String emailaddress;

    private String username;

    private int sex;

    private int status;

    private String avatarurl;

    private int age;

    private List<String> interests;
}
