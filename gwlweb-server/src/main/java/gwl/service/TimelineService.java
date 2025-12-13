package gwl.service;

import java.io.IOException;

import org.springframework.kafka.annotation.KafkaListener;

import com.google.firebase.messaging.FirebaseMessagingException;

import gwl.entity.event.TimelinePublishEvent;
import gwl.entity.event.TimelinePushEvent;
import gwl.pojo.DTO.TimelineDTO;

public interface TimelineService {
    /**
     * 推送帖子
     */
    void postTimeline(TimelineDTO TimelineDTO) throws java.io.IOException;

    /**
     * kafka推送消费者
     */
    public void onTimelinePush(TimelinePushEvent event) throws IOException, FirebaseMessagingException;
}
