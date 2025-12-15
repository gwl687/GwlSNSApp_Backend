package gwl.pojo.VO;

import java.util.List;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineVO {
    String userName;
    String context;
    String createdAt;
    List<String> imgUrls;
    //点赞总数
    Long likeCount;
    
}
