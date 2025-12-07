package gwl.controller.timeline;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    Result<String> postTimeline(@RequestBody TimelineDTO timelineDTO) {
        log.info("发帖子: {}",timelineDTO);
        try {
            timelineService.postTimeline(timelineDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success("posttimeline");
    }
}
