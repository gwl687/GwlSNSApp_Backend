package gwl.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.print.DocFlavor.STRING;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import gwl.components.ChannelManager;
import gwl.context.BaseContext;
import gwl.entity.TimelineContent;
import gwl.entity.User;
import gwl.entity.event.TimelinePublishEvent;
import gwl.entity.event.TimelinePushEvent;
import gwl.mapper.TimelineMapper;
import gwl.mapper.UserMapper;
import gwl.pojo.DTO.TimelineDTO;
import gwl.service.CommonService;
import gwl.service.TimelineService;
import gwl.service.UserService;
import io.micrometer.observation.Observation.CheckedRunnable;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;

@Service
public class TimelineServiceImpl implements TimelineService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TimelineMapper timelineMapper;
    @Autowired
    private ChannelManager channelManager;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private CommonService commonService;
   // @Autowired
    //private StringRedis redis;

    /**
     * 推送帖子
     * 
     * @throws IOException
     */
    @Override
    public void postTimeline(TimelineDTO timelineDTO) throws IOException {
        // 添加到数据库
        TimelineContent timelineContent = TimelineContent.builder()
                .userId(timelineDTO.getUserId())
                .context(timelineDTO.getContext())
                .imgUrls(null)
                .build();
        Long postId = timelineMapper.postTimeline(timelineContent);
        // 存图片到S3
        for (MultipartFile file : timelineDTO.getFiles()) {
            commonService.uploadToS3(file, file.getName(), "timeline/" + postId);
        }
        // 拿到s3的图片url,更新mysql
        List<String> imgUrls = commonService.getS3ImgUrls("timeline/" + postId);
        timelineMapper.updateTimelineImgs(imgUrls);

        // 推送
        List<User> friends = userMapper.getFriendListByUserId(BaseContext.getCurrentId());
        List<Long> friendIds = new ArrayList<>();
        for (User f : friends) {
            friendIds.add(f.getId());
        }
        Long timelineId = timelineMapper.postTimeline(timelineDTO);
        TimelinePublishEvent event = new TimelinePublishEvent(
                BaseContext.getCurrentId(),
                timelineId);

        kafkaTemplate.send("timeline_publish", event);
    }

    // 发帖的消费者
    @Override
    @KafkaListener(topics = "timeline_publish", groupId = "timeline-group")
    public void onTimelinePublish(TimelinePublishEvent event) {

    }

    // 推送给粉丝的消费者
    @Override
    @KafkaListener(topics = "timeline_push", groupId = "timeline-group")
    public void onTimelinePush(TimelinePushEvent event) {
        // 异步推送给多个好友
        // channelManager.sendCommand(event.getUserId(), event.getFriendIds(),
        // "timelinepublish");

    }
}
