package gwl.pojo.vo;

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
public class FriendListVO {
    @Schema(description = "用户名")
    private String userName;
    @Schema(description = "主键值")
    private Long id;
}
