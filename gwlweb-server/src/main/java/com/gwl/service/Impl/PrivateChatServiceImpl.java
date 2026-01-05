package com.gwl.service.Impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.gwl.mapper.FriendMapper;
import com.gwl.service.PrivateChatService;

public class PrivateChatServiceImpl implements PrivateChatService {
    @Autowired
    FriendMapper friendMapper;

    
}
