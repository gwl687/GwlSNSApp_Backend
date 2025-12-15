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
     * 查询是否被我点击了喜欢
     * 
     * @param imgUrls
     * @param postId
     */

    /**
     * 更新mysql里图片url
     */
    @Update("update timeline_content set img_urls=#{imgUrls,typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler} where id = #{postId}")
    void updateTimelineImgs(List<String> imgUrls, Long postId);
}
