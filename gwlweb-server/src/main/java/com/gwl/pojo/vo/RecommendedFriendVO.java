package com.gwl.pojo.vo;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedFriendVO {
    private Long userId;

    private int sex;

    private int age;

    private String username;

    private String avatarurl;

    private String emailaddress;

    private List<String> interests;
}
