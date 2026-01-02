package gwl.pojo.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineDTO {
    private Long userId;
    private String context;
    private List<MultipartFile> files;
}
