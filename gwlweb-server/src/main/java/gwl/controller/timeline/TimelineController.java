package gwl.controller.timeline;

import java.io.IOException;
import java.util.List;

import org.checkerframework.checker.units.qual.t;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gwl.entity.timeline.TimelineUserLike;
import gwl.pojo.DTO.PostCommentDTO;
import gwl.pojo.DTO.TimelineDTO;
import gwl.pojo.DTO.TimelineHitLikeDTO;
import gwl.pojo.VO.TimelineVO;
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
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        TimelineDTO timelineDTO = TimelineDTO.builder()
                .userId(userId)
                .context(context)
                .files(files).build();
        log.info("发帖子: {}", timelineDTO);
        try {
            timelineService.postTimeline(timelineDTO);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Result.success("posttimeline");
    }

    /**
     * 获取timeline内容
     * 
     * @return
     */
    @GetMapping(path = "gettimelinepost", produces = "application/json")
    Result<List<TimelineVO>> getTimelinePost() {
        return Result.success(timelineService.getTimelinePost());
    }

    /**
     * 获取指定timeline内容
     * 
     * @return
     */
    @GetMapping(path = "gettimelinepostbytimelindid", produces = "application/json")
    Result<TimelineVO> getTimelinePostByTimelineId(@RequestParam Long timelineId) {
        return Result.success(timelineService.getTimelinePostByTimelineId(timelineId));
    }


    /**
     * 给帖子点赞
     * 
     * @return
     */
    @PostMapping(path = "hitlike", produces = "application/json")
    Result<String> timelinehitLike(@RequestBody Long timelineId) {
        try {
            timelineService.likeHit(timelineId);
        } catch (Exception e) {
            return Result.error(e.toString());
        }
        return Result.success("hitlike succees!");
    }

    /**
     * 给帖子评论
     * 
     * @return
     */
    @PostMapping(path = "postcomment", produces = "application/json")
    Result<Boolean> postComment(@RequestBody PostCommentDTO postCommentDTO) {
        timelineService.postComment(postCommentDTO);
        return Result.success(true);
    }
}
