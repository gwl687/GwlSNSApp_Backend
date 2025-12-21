package gwl.entity.event;

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
    private String publisherName;
    private String content;
    private LocalDateTime createdAt;
    private Long postId;
    private List<String> fanIds;
}
