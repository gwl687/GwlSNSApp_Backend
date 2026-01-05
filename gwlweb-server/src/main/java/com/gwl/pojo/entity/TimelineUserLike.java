package com.gwl.pojo.entity;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineUserLike {
    Long timelineId;
    Map<Long, Integer> userLikeCount;
}
