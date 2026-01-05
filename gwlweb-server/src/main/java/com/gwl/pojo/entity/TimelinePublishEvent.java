package com.gwl.pojo.entity;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimelinePublishEvent {
    private Long userId;         // 谁发的动态
    private Long timelineId;     // 动态ID
    //private List<Long> friendIds; // 要通知哪些好友
}