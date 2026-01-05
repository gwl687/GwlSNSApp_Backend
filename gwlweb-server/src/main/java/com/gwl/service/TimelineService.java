package com.gwl.service;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.RequestParam;

import com.google.firebase.messaging.FirebaseMessagingException;

import com.gwl.pojo.dto.PostCommentDTO;
import com.gwl.pojo.dto.TimelineDTO;
import com.gwl.pojo.entity.TimelineLikeHitEvent;
import com.gwl.pojo.entity.TimelinePushEvent;
import com.gwl.pojo.vo.TimelineVO;

public interface TimelineService {
    /**
     * 推送帖子
     */
    void postTimeline(TimelineDTO TimelineDTO);
    /**
     * 获取帖子列表(刷新)
     */
    List<TimelineVO> getTimelinePost(Integer limit, Instant cursor);
    /**
     * 获取单个帖子
     * @return
     */
    TimelineVO getTimelinePostByTimelineId(Long timelineId);

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
