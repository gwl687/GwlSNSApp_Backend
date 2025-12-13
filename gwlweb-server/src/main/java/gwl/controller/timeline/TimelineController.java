package gwl.controller.timeline;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gwl.pojo.DTO.TimelineDTO;
import gwl.result.Result;
import gwl.service.TimelineService;
import gwl.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/timeline")
@Slf4j
@Tag(name = "帖子相关接口")
public class TimelineController {
    @Autowired
    TimelineService timelineService;

    @PostMapping(path = "posttimeline", produces = "application/json")
    Result<String> postTimeline(@RequestParam("userId") Long userId,
            @RequestParam("context") String context,
            @RequestParam("createTime") String createTime,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        TimelineDTO timelineDTO = TimelineDTO.builder()
                .userId(userId)
                .context(context)
                .createTime(createTime)
                .files(files != null ? files : null).build();
        log.info("发帖子: {}", timelineDTO);
        try {
            timelineService.postTimeline(timelineDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success("posttimeline");
    }
}
