package com.gwl.pojo.vo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.gwl.pojo.entity.TimelineComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineVO {
    String userName;
    String context;
    Instant createdAt;
    List<String> imgUrls;
    //点赞总数
    Integer totalLikeCount;
    Integer likedByMeCount;
    Long timelineId;
    List<LikeUserVO> topLikeUsers;
    List<TimelineComment> comments;
}
