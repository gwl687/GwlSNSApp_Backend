package gwl.pojo.VO;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "创建群组VO")
public class createGroupChatVO {
    @Schema(description = "群id")
    private Long groupId;
    @Schema(description = "群成员id")
    private List<Long> memberIds;
}
