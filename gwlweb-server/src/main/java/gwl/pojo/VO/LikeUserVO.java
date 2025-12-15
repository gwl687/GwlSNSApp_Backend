package gwl.pojo.VO;

import com.google.auto.value.AutoValue.Builder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LikeUserVO {
    private Long userId; 
    private String avatarUrl; 
    private int userLikeCount; 
}
