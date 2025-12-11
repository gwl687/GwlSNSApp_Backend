package gwl.entity.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelinePushEvent {
    private Long FanId; 
    private Long UserId;
}
