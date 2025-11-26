package gwl.pojo.DTO;

import java.util.List;

import lombok.Data;

@Data
public class TimelineDTO {
    private Long userId;
    private String content;
    private List<String> imgUrls;
}
