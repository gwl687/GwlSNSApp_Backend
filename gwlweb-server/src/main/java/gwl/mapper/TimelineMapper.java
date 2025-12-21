package gwl.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import gwl.entity.TimelineContent;
import gwl.entity.timeline.TimelineComment;
import gwl.entity.timeline.TimelineUserLike;
import gwl.pojo.DTO.PostCommentDTO;
import gwl.pojo.DTO.TimelineDTO;
import gwl.pojo.VO.TimelineContentVO;
import gwl.pojo.VO.TimelineVO;

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
    void addTimeline(Long userId, Long postId, String createdAt);

    /**
     * 获取用户timeline ids
     * 
     * @return
     */
    @Select("select post_id from timeline where user_id = #{userId} order by created_at desc")
    List<Long> getTimeline(Long userId);

    /**
     * 获取timeline内容
     * 
     * @return
     */
    // @Select("select tu.username ,tc.context,tc.img_urls,tc.created_at from
    // timeline_content tc left join test_user tu on tc.user_id = tu.id where tc.id
    // = #{timelineId}")
    // @Results({
    // @Result(column = "img_urls", property = "imgUrls", typeHandler =
    // JacksonTypeHandler.class)
    // })
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
    @Insert("insert into timeline_comment(timeline_id,user_id,comment) values(#{timelineId},#{userId},#{comment})")
    Long postComment(TimelineComment timelineComment);

    /**
     * 获取帖子评论
     * 
     * @param timelineId
     */
    @Select("select id as commentId,user_id as userId,comment,timeline_id as timelineId,created_at as createdAt from timeline_comment where timeline_id = #{timelineId} LIMIT 10")
    List<TimelineComment> getComments(Long timelineId);
}
