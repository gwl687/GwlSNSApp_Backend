package gwl.pojo.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineContent {
    private Long id;
    private Long userId;
    private String context;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> imgUrls;
}
