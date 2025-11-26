package gwl.pojo.VO;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfoVO implements Serializable{
    private String username;

    private String sex;

    private String avatarurl;

    private String emailaddress;
}
