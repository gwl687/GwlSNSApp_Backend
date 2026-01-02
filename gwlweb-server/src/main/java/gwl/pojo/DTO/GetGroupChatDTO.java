package gwl.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class GetGroupChatDTO {
     @Schema(description = "群id")
    Long groupId;
}
