package com.gwl.pojo.vo;

import lombok.Data;

@Data
public class GroupMessagesVO {
    Long id;
    Long groupId;
    Long senderId;
    String content;
    String type;
}
