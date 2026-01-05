package gwl.pojo.entity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelinePushEvent {
    private Long fromUser;
    private String publisherName;
    private String content;
    private Instant createdAt;
    private Long postId;
    private List<Long> fanIds;
}
