package gwl.pojo.dto;

import lombok.Data;

@Data
public class GroupmessageDTO {
    Long id;
    Long groupId;
    Long senderId;
    String content;
    String type;
}
