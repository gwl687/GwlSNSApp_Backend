package gwl.service.Impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import event.TimelinePublishEvent;
import gwl.components.ChannelManager;
import gwl.context.BaseContext;
import gwl.entity.User;
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

    /**
     * 推送帖子
     * @throws IOException 
     */
    @Override
    public void postTimeline(TimelineDTO timelineDTO) throws IOException {
        //添加到数据库


        // 存图片到S3
        for (MultipartFile file : timelineDTO.getFiles()) {

            commonService.uploadToS3(file, file.getName(),"timeline");
        }
        // 推送
        List<User> friends = userMapper.getFriendListByUserId(BaseContext.getCurrentId());
        List<Long> friendIds = new ArrayList<>();
        for (User f : friends) {
            friendIds.add(f.getId());
        }
        Long timelineId = timelineMapper.postTimeline(timelineDTO);
        TimelinePublishEvent event = new TimelinePublishEvent(
                BaseContext.getCurrentId(),
                timelineId,
                friendIds);

        kafkaTemplate.send("timeline_publish", event);
    }

    @Override
    @KafkaListener(topics = "timeline_publish", groupId = "timeline-group")
    public void onTimelinePublish(TimelinePublishEvent event) {
        // 异步推送给多个好友
        channelManager.sendCommand(event.getUserId(), event.getFriendIds(), "timelinepublish");

    }

}
