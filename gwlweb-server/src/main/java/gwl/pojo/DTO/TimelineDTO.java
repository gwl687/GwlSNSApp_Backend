package gwl.pojo.DTO;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class TimelineDTO {
    private Long userId;
    private String context;
    private List<MultipartFile> files;
}
