package gwl.service;

import java.io.IOException;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;

import com.google.firebase.messaging.FirebaseMessagingException;

import gwl.entity.event.TimelinePublishEvent;
import gwl.entity.event.TimelinePushEvent;
import gwl.pojo.DTO.TimelineDTO;
import gwl.pojo.VO.TimelineVO;

public interface TimelineService {
    /**
     * 推送帖子
     */
    void postTimeline(TimelineDTO TimelineDTO) throws java.io.IOException;
    /**
     * 获取帖子(刷新)
     */
    List<TimelineVO> getTimelinePost();

    /**
     * kafka推送消费者
     */
    public void onTimelinePush(TimelinePushEvent event) throws IOException, FirebaseMessagingException;
}
