package gwl.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;

import gwl.pojo.DTO.TimelineDTO;

@Mapper
public interface TimelineMapper {
    /**
     * 推送帖子
     */
    @Insert("insert into timeline (user_id,content,imgurls) values (#{userId},#{content},#{imgUrls,typeHandler=com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler})")
    Long postTimeline(TimelineDTO timelineDTO);
}
