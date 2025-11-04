package gwl.pojo.VO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "获取群信息VO")
public class GroupChatVO {
    Long groupId;
    String groupName;
    Long ownerId;
    String avatarUrl;
    List<Long> memberIds;
}
