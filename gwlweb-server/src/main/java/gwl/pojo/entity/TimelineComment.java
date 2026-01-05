package gwl.pojo.entity;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineComment {
    Long commentId;
    Long timelineId;
    Long userId;
    String comment;
    Instant createdAt;
}
