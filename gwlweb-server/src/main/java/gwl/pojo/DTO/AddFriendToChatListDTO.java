package gwl.pojo.DTO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class AddFriendToChatListDTO {
    @Schema(description = "朋友或群的id")
    private Long userId;
    @Schema(description = "朋友或群的id")
    private Long friendId;
    @Schema(description = "是否为群聊")
    private boolean isGroup;
}
