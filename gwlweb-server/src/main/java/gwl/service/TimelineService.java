package gwl.service;

import org.springframework.kafka.annotation.KafkaListener;

import event.TimelinePublishEvent;
import gwl.pojo.DTO.TimelineDTO;

public interface TimelineService {
    /**
     * 推送帖子
     */
    void postTimeline(TimelineDTO TimelineDTO) throws java.io.IOException;


   /**
    * 卡夫卡消费者
    * @param event
    */
    public void onTimelinePublish(TimelinePublishEvent event);

}
