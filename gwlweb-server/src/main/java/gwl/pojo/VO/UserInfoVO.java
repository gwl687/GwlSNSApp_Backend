package gwl.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO{
    private Long userId;

    private int sex;

    private String username;

    private String avatarurl;

    private String emailaddress;
}
