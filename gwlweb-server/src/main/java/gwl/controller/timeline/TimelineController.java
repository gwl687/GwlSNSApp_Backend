package gwl.controller.timeline;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import gwl.pojo.dto.PostCommentDTO;
import gwl.pojo.dto.TimelineDTO;
import gwl.pojo.vo.TimelineVO;
import gwl.result.Result;
import gwl.service.TimelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Int;

@RestController
@RequestMapping("/api/timeline")
@Slf4j
@Tag(name = "timeline")
public class TimelineController {
    @Autowired
    TimelineService timelineService;

    /**
     * post a timeline
     * 
     * @param userId
     * @param context
     * @param files
     * @return
     */
    @PostMapping(path = "posttimeline", produces = "application/json")
    @Operation(summary = "post a timeline")
    Result<String> postTimeline(@RequestParam("userId") Long userId,
            @RequestParam("context") String context,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        TimelineDTO timelineDTO = TimelineDTO.builder()
                .userId(userId)
                .context(context)
                .files(files).build();
        log.info("post a timeline: {}", timelineDTO);
        timelineService.postTimeline(timelineDTO);
        return Result.success("posttimeline");
    }

    /**
     * 获取timeline内容
     * 
     * @return
     */
    @GetMapping(path = "gettimelinepost", produces = "application/json")
    @Operation(summary = "get all timelines content")
    Result<List<TimelineVO>> getTimelinePost(@RequestParam Integer limit,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime cursor) {
        return Result.success(timelineService.getTimelinePost(limit, cursor));
    }

    /**
     * 获取指定timeline内容
     * 
     * @return
     */
    @GetMapping(path = "gettimelinepostbytimelindid", produces = "application/json")
    @Operation(summary = "get timeline content by timelineId")
    Result<TimelineVO> getTimelinePostByTimelineId(@RequestParam Long timelineId) {
        return Result.success(timelineService.getTimelinePostByTimelineId(timelineId));
    }

    /**
     * 给帖子点赞
     * 
     * @return
     */
    @PostMapping(path = "hitlike", produces = "application/json")
    @Operation(summary = "hitlike a timeline")
    Result<String> timelinehitLike(@RequestBody Long timelineId) {
        timelineService.likeHit(timelineId);
        return Result.success("hitlike success!");
    }

    /**
     * 给帖子评论
     * 
     * @return
     */
    @PostMapping(path = "postcomment", produces = "application/json")
    @Operation(summary = "post a comment")
    Result<Boolean> postComment(@RequestBody PostCommentDTO postCommentDTO) {
        timelineService.postComment(postCommentDTO);
        return Result.success(true);
    }
}
