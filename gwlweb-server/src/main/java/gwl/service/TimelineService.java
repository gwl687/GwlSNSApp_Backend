package gwl.service;

import java.io.IOException;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;

import com.google.firebase.messaging.FirebaseMessagingException;

import gwl.entity.event.TimelineLikeHitEvent;
import gwl.entity.event.TimelinePublishEvent;
import gwl.entity.event.TimelinePushEvent;
import gwl.pojo.DTO.PostCommentDTO;
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
     * 给帖子点赞
     */
    void likeHit(Long timelineId);

    /**
     * kafka点赞消费者
     */
    void onLikeHit(@Payload TimelineLikeHitEvent event);

    /**
     * 刷到mysql
     */
    void flushTimelineLikeToMySQL();

    /**
     * kafka推送消费者
     */
    void onTimelinePush(TimelinePushEvent event) throws IOException, FirebaseMessagingException;
    /**
     * 给帖子评论
     */
    void postComment(PostCommentDTO postCommentDTO);
}
