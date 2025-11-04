package gwl.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupChat {
    Long groupId;
    String groupName;
    Long ownerId;
    String avatarUrl;
    String memberIds;
}
