package gwl.service.Impl;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import event.TimelinePublishEvent;
import gwl.components.ChannelManager;
import gwl.context.BaseContext;
import gwl.entity.User;
import gwl.mapper.TimelineMapper;
import gwl.mapper.UserMapper;
import gwl.pojo.DTO.TimelineDTO;
import gwl.service.TimelineService;
import io.micrometer.observation.Observation.CheckedRunnable;

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

    /**
     * 推送帖子
     */
    @Override
    public void postTimeline(TimelineDTO timelineDTO) {
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

    @KafkaListener(topics = "timeline_publish", groupId = "timeline-group")
    public void onTimelinePublish(TimelinePublishEvent event) {

        // 异步推送给多个好友
        channelManager.sendCommand(event.getFriendIds(), "timelinepublish");

    }
}
