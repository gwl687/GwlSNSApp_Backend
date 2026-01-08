package com.gwl.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.cglib.core.Local;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import com.gwl.pojo.dto.PostCommentDTO;
import com.gwl.pojo.dto.TimelineDTO;
import com.gwl.pojo.entity.TimelineComment;
import com.gwl.pojo.entity.TimelineContent;
import com.gwl.pojo.entity.TimelineUserLike;
import com.gwl.pojo.vo.TimelineContentVO;
import com.gwl.pojo.vo.TimelineVO;

@Mapper
public interface TimelineMapper {
    /**
     * 推送timeline
     */
    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into timeline_content (user_id,context,img_urls) values (#{userId},#{context},#{imgUrls,typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler})")
    void postTimeline(TimelineContent timelineContent);

    /**
     * 用户timeline表新增数据
     */
    @Insert("insert into timeline(user_id,post_id,created_at) values (#{userId},#{postId},#{createdAt})")
    void addTimeline(Long userId, Long postId, Instant createdAt);

    /**
     * 获取用户timeline ids
     * 
     * @return
     */
    List<Long> getTimelineIds(Long userId, Integer limit, Instant cursorTime,Long cursorId);

    /**
     * 获取timeline内容
     * 
     * @return
     */
    TimelineVO getTimelineContent(Long timelineId, Long currentUserId);

    /**
     * 点击喜欢的刷盘
     * 
     * @param imgUrls
     * @param postId
     */
    void flushLikeToDB(TimelineUserLike timelineUserLike);

    /**
     * 更新mysql里图片url
     */
    @Update("update timeline_content set img_urls=#{imgUrls,typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler} where id = #{postId}")
    void updateTimelineImgs(List<String> imgUrls, Long postId);

    /**
     * 给帖子评论
     */
    @Options(useGeneratedKeys = true, keyProperty = "commentId", keyColumn = "id")
    @Insert("insert into timeline_comment(timeline_id,user_id,comment,created_at) values(#{timelineId},#{userId},#{comment},#{createdAt})")
    int postComment(TimelineComment timelineComment);

    /**
     * 获取帖子评论
     * 
     * @param timelineId
     */
    @Select("select id as commentId,user_id as userId,comment,timeline_id as timelineId,created_at as createdAt from timeline_comment where timeline_id = #{timelineId} LIMIT 10")
    List<TimelineComment> getComments(Long timelineId);
}
