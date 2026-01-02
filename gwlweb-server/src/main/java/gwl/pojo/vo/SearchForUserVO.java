package gwl.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchForUserVO {
    private Long userId;
    private Integer sex;
    private String username;
    private String avatarurl;
    private String emailaddress;
    private Integer status;
}
