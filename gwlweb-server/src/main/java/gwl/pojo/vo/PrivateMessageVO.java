package gwl.pojo.vo;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrivateMessageVO {
    Long id;
    Long receiverId;
    Long senderId;
    String content;
    String type;
    LocalDateTime createTime;
}
