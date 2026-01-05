package com.gwl.pojo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录VO")
public class UserLoginVO {
    @Schema(description = "用户id")
    private Long id;
    @Schema(description = "邮箱地址")
    private String emailaddress;
    @Schema(description = "jwt令牌")
    private String token;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "avatarUrl")
    private String avatarUrl;
}
