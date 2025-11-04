package gwl.pojo.VO;

import lombok.Data;

@Data
public class UpdateUserInfoVO {

    private String username;

    private String name;

    private String sex;

    private int status;

    private String  avatarurl;
}
