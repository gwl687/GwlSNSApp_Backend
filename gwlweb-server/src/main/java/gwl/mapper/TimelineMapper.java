package gwl.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import gwl.entity.TimelineContent;
import gwl.pojo.DTO.TimelineDTO;

@Mapper
public interface TimelineMapper {
    /**
     * 推送帖子
     */
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    @Insert("insert into timeline_content (user_id,context,img_urls,create_time) values (#{userId},#{context},#{imgUrls,typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler},#{createTime})")
    Long postTimeline(TimelineContent timelineContent);

    /**
     * 更新mysql里图片url
     */
    @Update("update timeline_content set img_urls=#{imgUrls,typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler}")
    void updateTimelineImgs(List<String> imgUrls);
}
