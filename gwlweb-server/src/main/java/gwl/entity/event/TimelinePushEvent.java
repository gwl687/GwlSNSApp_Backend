package gwl.entity.event;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelinePushEvent {
    private String publisherName;
    private String content;
    private Long postId;
    private List<String> FanIds; 
}
