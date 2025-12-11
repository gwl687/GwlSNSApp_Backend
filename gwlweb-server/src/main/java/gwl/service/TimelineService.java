package gwl.service;

import org.springframework.kafka.annotation.KafkaListener;

import gwl.entity.event.TimelinePublishEvent;
import gwl.entity.event.TimelinePushEvent;
import gwl.pojo.DTO.TimelineDTO;

public interface TimelineService {
    /**
     * 推送帖子
     */
    void postTimeline(TimelineDTO TimelineDTO) throws java.io.IOException;

    /**
     * kafka发帖消费者
     * 
     * @param event
     */
    public void onTimelinePublish(TimelinePublishEvent event);
    /**
     * kafka推送消费者
     */
    public void onTimelinePush(TimelinePushEvent event);
}
