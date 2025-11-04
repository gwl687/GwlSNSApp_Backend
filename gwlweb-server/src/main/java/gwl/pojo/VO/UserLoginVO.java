package gwl.pojo.VO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "获取好友列表VO")
public class UserLoginVO {
    @Schema(description = "姓名")
    private String name;
    @Schema(description = "jwt令牌")
    private String token;
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "avatarUrl")
    private String avatarUrl;
    @Schema(description = "主键值")
    private Long id;
}
